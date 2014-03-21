#lang racket
(require "MIRTOlib.rkt")

;; ---------------------------------------------------------
;; Maze exploration using left turn choices
;; ---------------------------------------------------------


(define previousTime (current-inexact-milliseconds))


;; Value for sensors
(define currentTime 0)

;; Value for sensors
(define valueMiddle 0)
(define valueLeft 0)
(define valueRight 0)

;; Above this value, we are on black.
;; Otherwise, we are on white
(define threshold 1200)

;; A position is described by the values of the 3 sensors. 
;; 0 means black (IR reading > threshold)
;; 1 means white (IR reading <= threshold)
;; There are 8 valid positions:
(define positions (list
                   '(0 0 0) ;; all three sensors on black
                   '(1 1 0) ;; right sensor on black (we are to the left of the line)
                   '(1 0 1) ;; In the middle
                   '(1 0 0) ;; middle and right sensors on black
                   '(0 1 1) ;; left sensor on black (we are to the right of the line)
                   ;; (0 1 0) should not exist! If we get this, something is wrong...
                   '(0 0 1) ;; left and middle sensors on black
                   '(1 1 1) ;; all sensors on white
                   )
  ) ;; end of definition of possible positions

(printf "Positions defined\n")

;; In some positions we need to make a choice. 
(define choicePoints (list 
                      '(1 0 0) ;; this could be a right turn or a right split
                      '(0 0 1) ;; this could be a left turn or a left split
                      '(0 0 0) ;; this could be a T junction or a 4 way junction
                      )
  ) ;; end of definition of choice points


(printf "Choice points defined\n")



;; The idea is the following:
;; - If we are on (1 0 1), we keep going straight
;; - If we are on (1 1 0) or (0 1 1) we turn left (or right) to go back on the line
;; - If we are at a decision point: we store what we see (old position) and we move forward a little bit (new position)
;;   1) If old position = (1 0 0) and 1.1) new position = (1 1 1), it is a right turn
;;                                and 1.2) new position = (1 0 1), it is a right split
;;   2) If old position = (0 0 1) and 2.1) new position = (1 1 1), it is a left turn
;;                                and 2.2) new position = (1 0 1), it is a left split.
;;   3) If old position = (0 0 0) and 3.1) new position = (1 0 1), it is a 4 way junction
;;                                and 3.2) new position = (1 1 1), it is a T-junction
                      

;; A place to store the old position, initially all -1.
(define oldPosition (list -1 -1 -1))

;; the left motor counter, initially 0
(define leftCount 0)
;; the right motor counter 
(define rightCount 0)

;; The frequency of updates (in milliseconds)
(define interval 40)

;; The list of IR sensors (used in map below)
(define irSensors (list 1 2 3))

;; Store previous bumper(s) state
(define previousLeftBumper #f)
(define previousRightBumper #f)


;; Store previous states wrt the line
(define previousStateLeft #f)
(define previousStateRight #f)


;; Define directions states wrt the line
(define goingLeft #t)
(define goingRight #f)

;; just two speeds
(define SPEED-LOW 35)
(define SPEED-HIGH 45)
(define FWD-TICKS 19)


(define Kp 0.02) ;; the correction for proportional error
(define maxDelta SPEED-HIGH) ;; The max difference between the two motors


;; power ratio between left and right motor
(define LR 0.96)

(printf "All constants defined\n")

;; A utility fuction that returns 0 if x > threshold (defined above)
;; and returns 0 otherwise
(define (isWhite? x)
  (cond ( (> x threshold) 0)
        (else 1))
  )

(define irCutoffProportional 50) ;; the zero for proportional correction

;; A function to return the value of IR sensor num, set to 0 if
;; reading < irCutoff.
(define (IR num) 
  (define val (getIR num))
  (cond ( (< val irCutoffProportional) 0)
	(else val)
  )
)

;; The function to compute the error given the 3 readings and
;; the previous error (see PID following for additional comments)
(define (curError leftIR centerIR rightIR prevError)
  (cond ( (= 0 (+ leftIR centerIR rightIR)) 
          (cond [ (> prevError 0) 4000]
                [else 0]
                )
          )
        (else
	  (/ (+ (* centerIR 2000.0) (* rightIR 4000)) 
             (+ leftIR rightIR centerIR) )
        )
  )
) ;; end definition of curError

(define error 2000) ;; we start on the middle: FIXME?
(define prevError error)

;; A utility function to move forward a certain number of ticks
(define (forwardLoop ticks)
  (define curLeftCount (getCount 1))
  (define curRightCount (getCount 2))  
  ;; we stop one of the motors if it reaches one of the targets
  (cond ( (>= curLeftCount (+ leftCount ticks))
          ;; The first motor has done more than nrotations
          (w1-stopMotor)
          ) 
        ( (>= curRightCount (+ rightCount ticks))
          ;; The second motor has done more than nrotations
          (w2-stopMotor)
          )
        )  
  ;; Do not flood
  (sleep 0.02)  
  ;; we exit if we have reached the destination (- 2 ticks)
  (cond ( (or (< curLeftCount (- (+ leftCount ticks) 0)) 
              (< curRightCount (- (+ rightCount ticks) 0)))
         (forwardLoop ticks)
        )
  )
  (stopMotors)
) ;; end of forwardLoop


(printf "Forward loop defined\n")



;; A utility function to rotate ticks to the right
(define (rotate90Right ticks)
    (define curLeftCount (getCount 1))
    (define curRightCount (getCount 2))
    (cond ( (> curLeftCount (+ leftCount ticks))  ;; turning backward
            (w1-stopMotor)
          ) 
          ( (< curRightCount (- rightCount ticks))
            (w2-stopMotor)
          )
          ( (or (< curLeftCount (+ leftCount ticks)) (> curRightCount (- rightCount ticks)) )
            (rotate90Right ticks)
          )
          (else (stopMotors))
     )
) ;; end of rotate ticks to the right

;; A utility function to rotate ticks to the left
(define (rotate90Left ticks)
    (define curLeftCount (getCount 1))
    (define curRightCount (getCount 2))
    (cond ( (< curLeftCount (- leftCount ticks))  ;; turning backward
            (w1-stopMotor)
          ) 
          ( (> curRightCount (+ rightCount ticks))
            (w2-stopMotor)
          )
          ( (or (> curLeftCount (- leftCount ticks)) (< curRightCount (+ rightCount ticks)) )
            (rotate90Left ticks)
          )
          (else (stopMotors))
     )
) ;; end of rotate ticks to the right


;; A utility function to generate an espeak command
(define (say what)
  (string-append "espeak --stdout -s 150 -a 200 -ven+f3 \"" 
                 what "\" 2> /dev/null | aplay -q")
  )


;; A utility function to set the motors taking into account power balance LR (see above)
(define (fwd left right)
  (setMotors left (- 0 (* right LR)))
  )


;; A utility function to get the current position (expressed
;; as a list of 0 and 1)
(define (getPosition)
  (printf "L ~a M ~a R ~a\n" (getIR 3) (getIR 2) (getIR 1))
  (map isWhite? (list (getIR 3) (getIR 2) (getIR 1)))
  )

;; A utility function to check that bumpers are not pressed
(define (initial-wait) 
 (cond [(or (leftBump?) (rightBump?)) 
        (sleep 0.1) 
        (initial-wait)])
  )

(define currentPosition (list -1 -1 -1))

;; The main control loop
(define (controlLoop)
  
  (set! currentTime (current-inexact-milliseconds))
  
  ;; Do we have to do something now?
  (cond ( (> (- currentTime previousTime) interval)          

          (set! previousTime (current-inexact-milliseconds))
                    
          ;; This will return the current position (as defined above)
          (set! currentPosition (getPosition))
          (printf "CURRENT POSITION: ~a\n" currentPosition)
          (printf "OLD POSITION: ~a\n" oldPosition)
          
          (cond ( (member currentPosition choicePoints)
                  ;; We are at a point in which we need to make a decision
                  ;; We stop the motors, we move a bit forward and we take a decision
                  (set! oldPosition currentPosition)

                  (printf "ENTERING CHOICE POINT WITH ~a\n" currentPosition)
                  (stopMotors)
                  (system (say "I found a choice point. I am moving forward slowly..."))

                  (sleep 0.1)
                  
                  ;; We get the encoders
                  (set! leftCount (getCount 1))
                  (set! rightCount (getCount 2))
                  
                  ;; We start the motors (slow) and move forward 14 ticks.
                  (fwd SPEED-LOW SPEED-LOW)                  
                  (forwardLoop FWD-TICKS)
                  (stopMotors)
                  (sleep 0.1)
                  ;; Let's check the new position
                  (set! oldPosition currentPosition)
                  (set! currentPosition (getPosition))
                  (printf "NEW CURRENT POSITION: ~a\n" currentPosition)
                  
                  ;; We just do what we said above (see comments at the beginning of file)
                  (cond (                         
                         (equal? oldPosition (list 0 0 1) ) ;; either left turn or left split
                         (cond ( (equal? currentPosition '(1 1 1))
                                  ;; it was a left turn
                                  (system (say "I found a left turn. I am turning left"))
                                  (set! leftCount (getCount 1))
                                  (set! rightCount (getCount 2))
                                  (fwd (- 0 SPEED-LOW) SPEED-LOW)
                                  (rotate90Left 29)
                                  (stopMotors)
                                  (system (say "Turn completed, I am now going to move forward"))
                                  (fwd SPEED-HIGH SPEED-HIGH)
                                  ) ;; end of left turn
                                ( (equal? currentPosition '(1 0 1) )
                                   ;; it was a left split
                                  (system (say "I found a left split. I am turning left"))
                                  (set! leftCount (getCount 1))
                                  (set! rightCount (getCount 2))
                                  (fwd (- 0 SPEED-LOW) SPEED-LOW)
                                  (rotate90Left 29)
                                  (stopMotors)
                                  (system (say "Turn completed, I am now going to move forward"))
                                  (fwd SPEED-HIGH SPEED-HIGH)
                                  ) ;; end of left split
                                (else (system (say "I am lost, I found a position that I cannot understand")))
                                ) ;; end of options for (0 0 1)
                         ) ;; end of oldPosition = (0 0 1)
                        
                        
                        ( (equal? oldPosition (list 1 0 0) ) ;; either right turn or right split
                          (cond ( (equal? currentPosition '(1 1 1))
                                  ;; it was a right turn
                                  (system (say "I found a right turn. I am turning right"))
                                  (set! leftCount (getCount 1))
                                  (set! rightCount (getCount 2))
                                  (fwd SPEED-LOW (- 0 SPEED-LOW))
                                  (rotate90Right 28)
                                  (stopMotors)
                                  (system (say "Turn completed, I am now going to move forward"))
                                  (fwd SPEED-HIGH SPEED-HIGH)
                                  ) ;; end of right turn
                                ( (equal? currentPosition '(1 0 1) )
                                  ;; it was a right split
                                  (system (say "I found a right split. I am going straight"))
                                  (fwd SPEED-HIGH SPEED-HIGH)
                                  ) ;; end of left split
                                (else (system (say "I am lost, I found a position that I cannot understand")))
                                ) ;; end of options for (1 0 0)
                          ) ;; end of oldPosition = (1 0 0)
                        
                        ( (equal? oldPosition (list 0 0 0) ) ;; either right turn or right split
                          (cond ( (equal? currentPosition '(1 1 1))
                                  ;; it was a right turn
                                  (system (say "I found a T-junction. I am turning left"))
                                  (set! leftCount (getCount 1))
                                  (set! rightCount (getCount 2))
                                  (fwd (- 0 SPEED-LOW) SPEED-LOW)
                                  (rotate90Left 29)
                                  (stopMotors)
                                  (system (say "Turn completed, I am now going to move forward"))
                                  (fwd SPEED-HIGH SPEED-HIGH)
                                  ) ;; end of left turn
                                ( (equal? currentPosition '(1 0 1) )
                                  ;; it was a 4-way junction
                                  (system (say "I found a 4-way junction. I am turning left"))
                                  (set! leftCount (getCount 1))
                                  (set! rightCount (getCount 2))
                                  (fwd (- 0 SPEED-LOW) SPEED-LOW)
                                  (rotate90Left 29)
                                  (stopMotors)
                                  (system (say "Turn completed, I am now going to move forward"))
                                  ) ;; end of left split
                                (else (system (say "I am lost, I found a position that I cannot understand")))
                                ) ;; end of options for (0 0 0)
                          ) ;; end of oldPosition = (0 0 0)
                        (else ;; something went wrong... 
                         (system (say "I found strange value, I am moving straight"))
                         (fwd SPEED-LOW SPEED-LOW)
                         )
                        ) ;; end of position member of choicePoints
                  ) ;; end of position member of choicePoints
                (else ;; this is not a choice point
                 
                 ;; We apply a proportional correction, but if we are lost we do a U-turn
                 (define leftIR (IR 3))
                 (define rightIR (IR 1))
                 (define centerIR (IR 2))
                 (printf "Values are ~a ~a ~a\n" leftIR centerIR rightIR)
                 (set! error (curError leftIR centerIR rightIR prevError) )
                 (define proportional (- error 2000))
                 
                 (printf "PROPORTIONAL IS ~a\n" proportional)
                 
                 (cond ( (> (+ leftIR rightIR centerIR) 0)
                          (define correction (* Kp proportional))
                          
                          (define delta correction)
                          (cond ( (> correction maxDelta) (set! delta maxDelta))
                                ( (< correction (- 0 maxDelta)) (set! delta (- 0 maxDelta)))
                                )
                          (cond ( (< delta 0) ;; we are to the left
                                  (fwd (+ SPEED-HIGH delta) SPEED-HIGH))
                                (else ;; we are to the right
                                 (fwd SPEED-HIGH (- SPEED-HIGH delta) )
                                 )
                                )
                          ;; end of proportional correction
                          )
                         (else ;; we found a dead end?
                          
                          (printf "This could be a dead end, let's move forward slowly...\n")
                          (stopMotors)
                          (set! leftCount (getCount 1))
                          (set! rightCount (getCount 2))
                          (fwd SPEED-LOW SPEED-LOW)
                          (forwardLoop 12)
                          (stopMotors)
                          (cond ( (= 0 (+ leftIR rightIR centerIR)) ;; this is indeed a dead end
                                  (printf "I found a dead end, I am making a U-turn\n")
                                  (system (say "I found a dead end, I am making a U-turn"))
                                  (set! leftCount (getCount 1))
                                  (set! rightCount (getCount 2))
                                  (fwd (- 0 SPEED-LOW) SPEED-LOW)
                                  (rotate90Left 64)
                                  (stopMotors)
                                  (printf "U-turn completed, I am now going to move forward")
                                  (system (say "U-turn completed, I am now going to move forward"))
                                  (fwd SPEED-HIGH SPEED-HIGH)
                                  )
                                )
                          ) ;; end else (for dead end)
                         ) ;; end of cases for sum of IR=0
                 ) ;; end else for this is not a choice point
                ) ;; end of big cond on choice points
                (set! oldPosition currentPosition)
          ) ;; end of cond for timed loop
        ) ;; end of cond for timed loop (outer)
          
  
  (sleep 0.02)
  
  ;; Exit if one bump sensor is pressed
  (cond ((not (or (leftBump?) (rightBump?)))
         (controlLoop) 
         )
        )
) ;; end of controlLoop


(define (start)
  (printf "Starting...\n")
  (setup)  
  ;; let's take things easy...
  (sleep 0.2)
  (enableIR)
  (printf "I'm going to start in half a second\n")
  ;; half a second to sabilise
  (system (say "I'm going to start in half a second"))
  (sleep 0.5)  
  (fwd SPEED-HIGH SPEED-HIGH)
  (controlLoop)
  (stopMotors)
  (shutdown)
  )

(start)
