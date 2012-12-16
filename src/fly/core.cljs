(ns fly.core
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [crate.core]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log clj->js]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(defn mm-offset [$content]
  (let [$doc ($ js/document)]
    (-> $doc
        rxj/mousemove
        (rx/select
         (fn [value]
           (let [offset (.offset $content)
                 x (+ (- (.-clientX value) (.-left offset))
                      (.scrollLeft $doc))
                 y  (+ (- (.-clientY value) (.-top offset))
                       (.scrollTop $doc))]
             {:offset-x x :offset-y y}))))))

(defpartial letter-span [letter]
  [:span {:style "position:absolute"} letter])

(defn bind-letter [mm-offset $content letter i]
  (let [s ($ (letter-span letter))
        pos (fn [e]
              (j/css s {:top  (str (:offset-y e) "px")
                        :left (str (+ (:offset-x e) (* i 10) 15) "px")}))]
    (j/append $content s)
    (-> mm-offset
        (rx/delay (* i 100))
        (rx/subscribe pos))))

(defn ^:export main []
  (let [$content ($ "#main-content")
        text "time flies like an arrow"]
    (doseq [i (range 0 (count text))]
      (bind-letter (mm-offset $content) $content (get text i) i))))