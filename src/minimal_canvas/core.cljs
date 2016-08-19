(ns minimal-canvas.core
  (:require
   [cljs.core.async :refer [chan close!]])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))

(defn make-request-animation-frame
  "compose a function that is the r-a-f func. returns a function. This returned function takes a callback and ensures
  its called next frame"
  []
  (cond
   (.-requestAnimationFrame js/window)
   #(.requestAnimationFrame js/window %)

   (.-webkitRequestAnimationFrame js/window)
   #(.webkitRequestAnimationFrame js/window %)

   (.-mozRequestAnimationFrame js/window)
   #(.mozRequestAnimationFrame js/window %)

   (.-oRequestAnimationFrame js/window)
   #(.oRequestAnimationFrame js/window %)

   (.-msRequestAnimationFrame js/window)
   #(.msRequestAnimationFrame js/window %)

   :else
   #(.setTimeout js/window % (/ 1000 60))))

;; build the actual function
(def request-animation-frame (make-request-animation-frame))

(defn next-frame
  "returns a single use channel which closes on next frame callback.
  pulling from it waits exactly one frame. eg
  ```
  ;; wait one frame
  (<! (next-frame))
  ```"
  []
  (let [c (chan)]
    (request-animation-frame #(close! c))
    c))

(let [canvas (js/document.getElementById "my-canvas")
      ctx (.getContext canvas "2d")]
  (go
    (loop []
      (loop [c 0]
        (set! (.-fillStyle ctx) "white")
        (.fillRect ctx 10 10 200 200)
        (set! (.-fillStyle ctx) "green")
        (.fillRect ctx 10 10 c 200)
        (<! (next-frame))
        (when (< c 200) (recur (inc c))))
      (recur))))
(defn hex [n]
  (let [h (.toString (int n) 16)
        l (count h)]
    (case l
      0 "00"
      1 (str "0" h)
      2 h
      (subs h (- l 2)))))

(defn rgb-to-hash [[r g b]]
  (str "#" (hex r) (hex g) (hex b)))

(defn colour-blend [[rs gs bs] [re ge be] steps]
  (let [dr (/ (- re rs) steps)
        dg (/ (- ge gs) steps)
        db (/ (- be bs) steps)]
    (for [n (range steps)]
      [(+ rs (* n dr))
       (+ gs (* n dg))
       (+ bs (* n db))])))

(defn hash-blend [start end steps]
  (map rgb-to-hash (colour-blend start end steps)))

