(ns cljs-rx-examples.client.todo
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [crate.core :as crate]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(def log-pr (comp log pr-str))

(def next-id (atom 0))

(defn make-todo [title]
  {:id (swap! next-id inc)
   :title title
   :completed false})

(def todos [(-> (make-todo "Create a TodoMVC template")
                (assoc :completed true))
            (make-todo "Rule the web")])

(defpartial todo-li []
  [:li
   [:div.view
    [:input.toggle {:type "checkbox"}]
    [:label]
    [:button.destroy]]
   [:input.edit]])

(def $todo-list ($ :#todo-list))

(defn populate-todo-li [$todo {:keys [id title completed]}]
  (j/data $todo :id id)
  (when completed
    (j/add-class $todo :completed)
    (.prop ($ :.toggle $todo) "checked" true))
  (j/inner ($ "label" $todo) title)
  (j/val ($ :.edit $todo) title)
  $todo)

(defn main []
  (doseq [todo todos]
    (let [li (populate-todo-li ($ (todo-li)) todo)]
      (j/append $todo-list li))))
