(ns cljs-rx-examples.client.todo.model
  (:require [cljs-rx.observable :as rx]
            [cljs-rx.clojure :as rxclj]
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
    (rxclj/add! todos todo)
    todo))

(defn mark-completed [todo completed?]
  (rxclj/obs-assoc! todo :completed completed?))

(defn toggle-completed [completed?]
  (doseq [todo todos]
    (mark-completed todo completed?)))

(defn edit-title [todo title]
  (rxclj/obs-assoc! todo :title title))

(defn remove-todo [todo]
  (rxclj/remove! todos todo))

(defn clear-completed []
  (swap! todos #(vec (remove :completed %))))

(def total-count
  (-> todos
      (rx/select count)))

(def incomplete-count
  (-> todos
      (rx/select #(count (remove :completed %)))))

(def complete-count
  (-> todos
      (rx/select #(count (filter :completed %)))))

(def change-obs (rxclj/diff todos))
(def todo-added (rxclj/added change-obs))
(def todo-removed (rxclj/removed change-obs))

(def all-completed
  (-> todos
      (rx/select #(= (count todos)
                     (count (filter :completed todos))))
      rx/distinct-until-changed))
