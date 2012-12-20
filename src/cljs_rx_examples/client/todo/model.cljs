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

(defn toggle-completed [completed?]
  (doseq [todo todos]
    (mark-completed todo completed?)))

(defn mark-completed [todo completed?]
  (obs-assoc! todo :completed completed?))

(defn edit-title [todo title]
  (obs-assoc! todo :title title))

(defn remove-todo [todo]
  (remove! todos todo))

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

(def change-obs (rxclj/diff (observable todos)))
(def todo-added (rxclj/added change-obs))
(def todo-removed (rxclj/removed change-obs))

(def all-completed
  (-> todos observable
      rxclj/changed
      (rx/select #(= (count todos)
                     (count (filter :completed todos))))))