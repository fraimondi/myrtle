#lang racket

(require "MIRTOlib.rkt")

;; ---------------------------------------------------------
;; Simple testing functions for Myrtle Robot
;; See testMotors and testSensors at the end of the file
;; ---------------------------------------------------------



(define (front)
   (setMotors 75 -75)
   (sleep 0.5)
   (stopMotors)
)

(define (back)
   (setMotors -75 75)
   (sleep 0.5)
   (stopMotors)
)

(define (right)
   (setMotors 75 75)
   (sleep 0.5)
   (stopMotors)
)

(define (left)
   (setMotors -75 -75)
   (sleep 0.5)
   (stopMotors)
)

(define (testMotors)
  (setup "/dev/tty.usbmodem1421" "mac")
  (printf "A Simple test to check the motors and to read the encoders\n\n\n")
  (printf "Current readings for the encoders: Motor 1 ~a; Motor 2 -> ~a\n" (getCount 1) (getCount 2))
  (printf "Moving forward at 75% power\n")
  (front)
  (printf "Current readings for the encoders: Motor 1 -> ~a; Motor 2 -> ~a\n\n" (getCount 1) (getCount 2))
  (sleep 1)
  (printf "Now moving back at 75% power\n")
  (back)
  (printf "Current readings for the encoders: Motor 1 -> ~a; Motor 2 -> ~a\n\n" (getCount 1) (getCount 2))
  (sleep 1)
  (printf "Now moving left at 75% power\n")
  (left)
  (printf "Current readings for the encoders: Motor 1 -> ~a; Motor 2 -> ~a\n\n" (getCount 1) (getCount 2))
  (sleep 1)
  (printf "Now moving tight at 75% power\n")
  (right)
  (printf "Current readings for the encoders: Motor 1 -> ~a; Motor 2 -> ~a\n\n" (getCount 1) (getCount 2))
  (printf "All done, closing down\n")
  (shutdown)
  )

;; Parameters needed for testing the sensors (loop below)
(define previousTime (current-inexact-milliseconds))
(define currentTime 0)
(define interval 5000)
(define analogList (list 1 2 3))

(define (sensorsLoop)
  (set! currentTime (current-inexact-milliseconds))
  (cond ( (> (- currentTime previousTime) interval) 
            (map (λ (i) (printf "IR sensor ~a -> ~a; " i (getIR i))) analogList)
            (printf "\n")
            (set! previousTime (current-inexact-milliseconds))
            )
          )
  (cond ( (leftBump?) (printf "Left bump pressed\n"))
        ( (rightBump?) (printf "Right bump pressed\n"))
        )
  (cond ((not (and (leftBump?) (rightBump?)))
         (sensorsLoop) 
         )
        )
  )

(define (testSensors)
  (printf "A Simple test to check the bump and IR sensors\n")
  (printf "The test will display the reading of the 3 IR sensors every 5 seconds")
  (printf "Press left and right bumpers at the same time to quit\n")
  (setup "/dev/tty.usbmodem1421" "mac")
  (printf "Launching test\n")
  (sleep 1)
  (enableIR)
  (sensorsLoop)
  (printf "Test finished, all seems OK\n")
  (shutdown)
  )
  
          
