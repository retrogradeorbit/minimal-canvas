(ns minimal-canvas.core
  (:require ))

(let [canvas (js/document.getElementById "my-canvas")
      ctx (.getContext canvas "2d")]
  (set! (.-fillStyle ctx) "green")
  (.fillRect ctx 10 10 200 200))
