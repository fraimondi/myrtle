#lang racket


;; Created by: Franco Raimondi

;;    This program is free software: you can redistribute it and/or modify
;;    it under the terms of the GNU General Public License as published by
;;    the Free Software Foundation, either version 3 of the License, or
;;    (at your option) any later version.

;;    This program is distributed in the hope that it will be useful,
;;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;    GNU General Public License for more details.

;;    You should have received a copy of the GNU General Public License
;;    along with this program.  If not, see <http://www.gnu.org/licenses/>.

;; CHANGELOG
;; 140128 Franco - New tests for Myrtle
;; 131125 Franco - Negative values cause problems. I'm now trying to use positive 
;;                 values only for pwr
;; 131122 Franco - Work started
;; This is a set of functions used to connect


;; Description
;; This is a simple library used to control two HUBee wheels mounted on an Arduino Uno
;; using an HUB-ee Prototyping Shield V1.1.
;; Most of this code is derive from the HUB-ee BMD-S Arduino Library
;; available at http://www.creative-robotics.com/?q=hubee-resources

;; TODO:
;; The code should be refactored, there is a lot of duplication!


;; TYPICAL USAGE: see simpleTest at the bottom of the file.


;; We talk to the Arduino using Firmata. 
(require "firmata.rkt")

(provide w1-stopMotor
         w2-stopMotor
         stopMotors
         setMotor
         setMotors
         setup
         shutdown
         enableIR
         rightBump?
         leftBump?
         getIR
         getCount
)
         
         
(define (w1-stopMotor)
  (send-sysex-int-msg #x7D 5 0)
)

(define (w2-stopMotor)
  (send-sysex-int-msg #x7D 6 0)
)

(define (stopMotors)
  (w1-stopMotor)
  (w2-stopMotor)
  )

;; A function to set motor1
(define (setMotor wheel speed)
  (define value (inexact->exact (floor (* speed 2.55))))
  (cond ( (< value -255) (set! value -255))
        ( (> value 255) (set! value 255))
        )
  ;;(printf "DEBUG: I'm sending this: ~a ~a\n" (+ 5 wheel) value)
  (send-sysex-int-msg #x7D (+ 5 wheel) value)  
) ;; end setMotor1

(define (setMotors speed1 speed2)
  ;;(printf "Setting motor 1: \n")
  (setMotor 0 speed1)
  ;;(printf "Setting motor 2: \n")
  (setMotor 1 speed2)
  )

;; IR sensors are attached to analog PINS 1,2,3 
;; but they need to be activated by setting PIN 14 to high
(define (enableIR)
  (set-pin-mode! 14 INPUT_MODE)
  (set-arduino-pin! 14)
  (report-analog-pin! 1 1)
  (report-analog-pin! 2 1)
  (report-analog-pin! 3 1)
)


;; Two Boolean functions to see if the bump is pressed
(define (leftBump?)
  (not (is-arduino-pin-set? 18))
  )

(define (rightBump?)
  (not (is-arduino-pin-set? 19))
  )

;; just to retrieve the analog reading of an IR sensor
;; I assume num = analog pin number
;; FIXME: implement error checking here
(define (getIR num)
  (read-analog-pin num)
)

;; Retrieve the count of the encoder for one motor
;; FIXME: check that num \in {1,2}
(define (getCount num)
  (cond ( (= num 1) 
          (motor1-read-count))
        ( (= num 2)
          (motor2-read-count)))
  )
;; A function to setup the connection.
;; You need to provide the port name, something like "/dev/ttyACM0" on linux
;; or "COM3" on Windows, and then the operating system (win, mac or linux, as a string)
(define (setup portname os)
  ;; Open Firmata port and stop motors
  (open-firmata portname os)
  (stopMotors)
  (motor1-reset-count)
  (motor2-reset-count)

  ;; Setting the PINs for bump switches
  (set-pin-mode! 18 INPUT_MODE)
  (set-pin-mode! 19 INPUT_MODE)
  
  ;; Enable reporting for various ports
  (report-digital-port! 0 1)
  (report-digital-port! 1 1)
  (report-digital-port! 2 1)
  (report-digital-port! 3 1)
  
  ;; Enable continuous reporting
  (send-sysex-msg #x7D 2)
  
  ) ;; end of setup

(define (shutdown)
  (stopMotors)
  (sleep 0.1)
  (close-firmata)
  )
