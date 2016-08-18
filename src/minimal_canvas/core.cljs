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
  (set! (.-fillStyle ctx) "green")
  (.fillRect ctx 10 10 200 200))
