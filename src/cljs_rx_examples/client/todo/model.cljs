(ns cljs-rx-examples.client.todo.model
  (:require [cljs-rx.observable :as rx]
            [cljs-rx.clojure :refer [add!
                                     remove!
                                     update!
                                     obs-assoc!
                                     observable] :as rxclj]
            [clojure.data :refer [diff]]
            [clojure.set :as set]
            [jayq.util :refer [log]]))

(def log-pr (comp log pr-str))

(def next-id (atom 0))

(def todos (rxclj/observable-vector []))

(defn new-todo [title]
  (let [todo (rxclj/observable-map
              {:id (swap! next-id inc)
               :title title
               :completed false})]
    (add! todos todo)
    todo))

(defn mark-completed [todo completed?]
  (obs-assoc! todo :completed completed?))

(defn remove-todo [todo]
  (update! todos #(vec (remove (partial = todo) %))))

(defn clear-completed []
  (update! todos #(vec (remove :completed %))))

(def total-count
  (-> todos observable
      (rx/select count)))

(def incomplete-count
  (-> todos observable
      (rx/select #(count (remove :completed %)))))

(def complete-count
  (-> todos observable
      (rx/select #(count (filter :completed %)))))

(def change-obs (rxclj/changed (observable todos)))
(def todo-added (rxclj/added change-obs))
(def todo-removed (rxclj/removed change-obs))

(rx/subscribe change-obs log-pr)
(rx/subscribe total-count log-pr)
;(rx/subscribe todo-removed log-pr)
