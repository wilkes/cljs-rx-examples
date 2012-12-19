(ns cljs-rx-examples.client.todo.model
  (:require [cljs-rx.observable :as rx]
            [cljs-rx.clojure :refer [as-obs] :as rxclj]
            [clojure.data :refer [diff]]
            [jayq.util :refer [log]]))

(def log-pr (comp log pr-str))

(def next-id (atom 0))

(def todos (rxclj/observable-vector []))

(defn new-todo [title]
  (let [todo (rxclj/observable-map
              {:id (swap! next-id inc)
               :title title
               :completed false})]
    (conj todos todo)
    todo))

(defn mark-completed [todo completed?]
  (assoc todo :completed completed?))

(def incomplete-count
  (-> todos as-obs
      (rx/select #(count (remove :completed %)))))

(rx/subscribe incomplete-count log-pr)
