(ns cljs-rx-examples.client.paint
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(defn mouse-drag [target]
  (let [mouse-up (rxj/mouseup target)
        mouse-move (rxj/mousemove target)]
    (rx/select-many (rxj/mousedown target)
                    (fn [_]
                      (-> mouse-move
                          (rx/select (juxt #(.-offsetX %) #(.-offsetY %)))
                          (rx/buffer-with-count 2 1)
                          (rx/select (juxt first second))
                          (rx/take-until mouse-up))))))

(defn ^:export main []
  (j/append ($ "#main-content")
            "<canvas id=\"canvas\" height=\"768\" width=\"1024\"/>")
  (let [canvas ($ :#canvas)
        ctx (.getContext (first canvas) "2d")
        drag (mouse-drag canvas)]
    (rx/subscribe drag
                  (fn [[[x1 y1] [x2 y2]]]
                    (.moveTo ctx x1 y1)
                    (.lineTo ctx x2 y2)
                    (.stroke ctx)))))