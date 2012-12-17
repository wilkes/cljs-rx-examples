(ns dd.core
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [crate.core]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log clj->js]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(defn- calc-offset [$target]
  (fn [event]
    (j/prevent event)
    {:left (- (.-clientX event)
              (-> $target j/offset :left))
     :top (- (.-clientY event)
             (-> $target j/offset :top))}))

(defn track-position [move up]
  (fn [{:keys [top left]}]
    (-> move
        (rx/select (fn [pos]
                     {:left (- (.-clientX pos) left)
                      :top (- (.-clientY pos) top)}))
        (rx/take-until up))))

(defn mouse-drag [$target]
  (let [mouseup (rxj/mouseup ($ js/document))
        mousemove (rxj/mousemove ($ js/document))]
    (-> (rxj/mousedown $target)
        (rx/select (calc-offset $target))
        (rx/select-many (track-position mousemove mouseup)))))

(defpartial drag-target []
  [:div#drag-target {:style "background-color: #000; border: 1px solid #666;color: #ffffff; padding: 10px; position: absolute; font-family: sans-serif; cursor: move"} "Drag Me!"])

(defn ^:export main []
  (let [$target ($ (drag-target))]
    (j/append ($ "#main-content") $target)
    (rx/subscribe (mouse-drag $target) (partial j/css $target))))