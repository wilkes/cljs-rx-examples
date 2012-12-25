(ns cljs-rx.dom
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [cljs-rx.clojure :refer [observable] :as rxclj]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log]]))

(def ENTER 13)

(defn input-entered [$input subscribe-fn]
  (-> $input
      rxj/keyup
      (rx/where #(= ENTER (.-keyCode %)))
      (rx/select #(j/val $input))
      (rx/subscribe subscribe-fn)))

(defn bind-inner [$elem source & [f]]
  (rx/subscribe source #(j/inner $elem ((or f identity) %))))

(defn bind-remove [source selector-fn]
  (rx/subscribe source #(doseq [x %]
                          (-> x selector-fn $ j/remove))))

(defn show [$elem source test-fn]
  (rx/subscribe source
                #(let [display-fn (if (test-fn %) j/show j/hide)]
                   (display-fn $elem))))

(defn checked [$elem source & [test-fn]]
  (let [test? (or test-fn identity)]
    (rx/subscribe source #(j/attr $elem :checked (test? %)))))

(defn checked? [$elem f]
  (-> $elem
      rxj/change
      rxj/select-checked
      (rx/subscribe f)))

(defn clicked? [$elem f]
  (-> $elem
      rxj/click
      (rx/subscribe f)))

(defn dblclicked? [$elem f]
  (-> $elem
      rxj/dblclick
      (rx/subscribe f)))