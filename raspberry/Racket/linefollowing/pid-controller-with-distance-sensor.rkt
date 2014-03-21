#lang racket

(require "MIRTOlib.rkt")

;; ---------------------------------------------------------
;; Line following with proportional + derivative correction
;; The idea is to compute the error using an approach
;; similar to this one: http://www.pololu.com/docs/0J18/19
;; In summary, we compute:
;; (leftIR*0 + centerIR*2000 + rightIR*4000)/(leftIR + centerIR + rightIR)
;; but with some corrections:
;; - if < 40, we set IR to 0 (white).
;; - when computing the value, if (leftIR+centerIR+rightIR)==0, we
;;   return the *previous* value of the error (so we know if we were
;;   to the right or to the left).
;; We aim at a value of 2000 and we correct the motors proportionally
;; to the error and proportionally to the rate of change of the error
;; ---------------------------------------------------------


;; ---------- Some constants definitions ---------------
;; Best config: PWR 100, Kp 0.05, Kd 1.6 maxDelta 75

(define PWR 90) ;; our target pwr on straight line
(define irCutoff 100) ;; below this value we set IR to 0
(define lr 0.92) ;; the correction for right motor
(define freq 50) ;; How often should we check? 
(define maxDelta 90) ;; The max difference between the two motors
(define Kp 0.03) ;; the correction for proportional error
(define Kd 1.4) ;; the correction for derivative of error
(define Ki 0.0001) ;; the correction for the integral of error


;; ---------- End of constants definitions -------------

;; ---------- Some utility functions -----------------

;; A function to move forward, taking into account correction
(define (fwd left right)
  (setMotors left (- 0 (* right lr)) )
)

;; A function to return the value of IR sensor num, set to 0 if
;; reading < irCutoff.
(define (IR num) 
  (define val (getIR num))
  (cond ( (< val irCutoff) 0)
	(else val)
  )
)

;; ---------- End of utility functions -----------


;; We now define the core functions

;; The function to compute the error given the 3 readings and
;; the previous error. See comment at the beginning of the file
(define (curError leftIR centerIR rightIR prevError)
  (cond ( (= 0 (+ leftIR centerIR rightIR)) prevError)
        (else
	  (/ (+ (* centerIR 2000.0) (* rightIR 4000)) 
             (+ leftIR rightIR centerIR) )
        )
  )
) ;; end definition of curError

(define previousTime (current-inexact-milliseconds))
(define currentTime (current-inexact-milliseconds))
(define error 2000) ;; we start on the middle: FIXME?
(define prevError error)
(define intError 0) ;; the integral of the error

(define (irLoop)

  (set! currentTime (current-inexact-milliseconds))
  (cond ( (> currentTime (+ previousTime freq))
      (define distance (getDistance) )
      (cond ( (and (> distance 0) (< distance 15)) 
              (stopMotors)
            )
      (else
  	  (define leftIR (IR 3))
  	  (define rightIR (IR 1))
  	  (define centerIR (IR 2))
          (set! error (curError leftIR centerIR rightIR prevError) )
          (define proportional (- error 2000))

          ;; Integral component: we reset to 0 when error is 0
          (cond ( (= 0 proportional) (set! intError 0))
	        (else (set! intError (+ intError proportional)))
          )
          ;; we assume dt constant, so this is just the difference
          ;; If derivative < 0, we moved to the left of the line
          (define derivative (- proportional (- prevError 2000)))
          (set! prevError error)
          ;; The correction is the sum of a proportional component,
          ;; integral component and a derivative component. 
          (define correction (+ (* Kp proportional)
                                (* Ki intError) 
	                        (* Kd derivative)) )

          (define delta correction)
          (cond ( (> correction maxDelta) (set! delta maxDelta))
                ( (< correction (- 0 maxDelta)) (set! delta (- 0 maxDelta)))
          )

;;          (printf "DEBUG: delta is ~a\n" delta)

          (cond ( (< delta 0) ;; we are to the left
                  (fwd (+ PWR delta) PWR))
                (else ;; we are to the right
                  (fwd PWR (- PWR delta) )
                )
          )
        )
     )
    )
  )
  (sleep 0.01)
  (requestDistance)
  (irLoop)


) ;; end of irLoop

(define (pi)
  (setup)
  (sleep 0.5)
  (enableIR)
  (sleep 0.5)
  (irLoop)
  (shutdown)
)

(printf "Starting...\n")
(pi)
