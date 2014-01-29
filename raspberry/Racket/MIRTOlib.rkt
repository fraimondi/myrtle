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


;; A function to setup the connection.
;; You need to provide the port name, something like "/dev/ttyACM0" on linux
;; or "COM3" on Windows.
(define (setup portname)
  (open-firmata portname "mac")
  (stopMotors)
  
  )


(define (left)
   (setMotors 80 -80)
   (sleep 0.5)
   (stopMotors)
)

(define (right)
   (setMotors -80 80)
   (sleep 0.5)
   (stopMotors)
)

(define (front)
   (setMotors 80 80)
   (sleep 0.5)
   (stopMotors)
)

(define (back)
   (setMotors -80 -80)
   (sleep 0.5)
   (stopMotors)
)

(define (test)
  (setup "/dev/tty.usbmodem1421")
  (front)
  (sleep 1)
;;  (send-sysex-msg #x7D 1)
  (back)
  (sleep 1)
;;  (send-sysex-msg #x7D 1)
  (left)
  (sleep 1)
;;  (send-sysex-msg #x7D 1)
  (right)
;;  (send-sysex-msg #x7D 1)
  )


