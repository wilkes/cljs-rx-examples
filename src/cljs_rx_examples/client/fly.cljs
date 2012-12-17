(ns cljs-rx-examples.client.fly
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [crate.core]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log clj->js]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(defn ->clj [val]
  (js->clj val :keywordize-keys true))

(defn mm-offset [$content]
  (let [$doc ($ js/document)]
    (-> (rxj/mousemove $doc)
        (rx/select ->clj)
        (rx/select
         (fn [{:keys [clientX clientY]}]
           (let [{:keys [top left]} (->clj (.offset $content))]
             {:x (+ (- clientX left) (.scrollLeft $doc))
              :y (+ (- clientY top) (.scrollTop $doc))}))))))

(defpartial letter-span [letter]
  [:span {:style "position:absolute;font-family: \"Lucida Console\", Monaco, monospace;"}
   letter])

(defn bind-letter [offset $content letter i]
  (let [s ($ (letter-span letter))
        pos (fn [{:keys [x y]}]
              (j/css s {:top  (str y "px")
                        :left (str (+ x (* i 10) 15) "px")}))]
    (j/append $content s)
    (-> offset
        (rx/delay (* i 50))
        (rx/subscribe pos))))

(defn ^:export main []
  (let [$content ($ "#main-content")
        text "TIME FLIES LIKE AN ARROW"
        offset (mm-offset $content)]
    (doseq [i (range 0 (count text))]
      (bind-letter offset $content (get text i) i))))