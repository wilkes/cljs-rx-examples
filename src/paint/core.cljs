(ns paint.core
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [crate.core]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(def log-pr (comp log pr-str))

(def $content ($ "#main-content"))

(defpartial content []
  [:canvas#canvas {:height 768 :width 1024}])

(defn ^:export main []
  (j/append $content (content))
  (let [canvas ($ :#canvas)
        ctx (.getContext (first canvas) "2d")
        mouse-diffs (-> canvas
                        rxj/mousemove
                        (rx/buffer-with-count 2 1)
                        (rx/select (fn [x]
                                     {:first (first x)
                                      :second (second x)})))
        mouse-button (rx/merge (-> canvas
                                   rxj/mousedown
                                   (rx/select (constantly true)))
                               (-> canvas
                                   rxj/mouseup
                                   (rx/select (constantly false))))
        paint (-> mouse-button
                  (rx/select (fn [down]
                               (if down
                                 mouse-diffs
                                 (rx/take mouse-diffs 0))))
                  rx/switch-latest)]
    (rx/subscribe paint (fn [x]
                          (.moveTo ctx
                                   (-> x :first .-offsetX)
                                   (-> x :first .-offsetY))
                          (.lineTo ctx
                                   (-> x :second .-offsetX)
                                   (-> x :second .-offsetY))
                          (.stroke ctx)))))