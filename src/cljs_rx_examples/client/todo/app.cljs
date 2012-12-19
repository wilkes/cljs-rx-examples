(ns cljs-rx-examples.client.todo.app
  (:require [cljs-rx-examples.client.todo.model :as model]
            [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [cljs-rx.clojure :refer [as-obs] :as rxclj]
            [clojure.set :as set]
            [crate.core :as crate]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(def log-pr (comp log pr-str))

(def $todo-list ($ :#todo-list))
(def $new-todo ($ :#new-todo))
(def $todo-count ($ :#todo-count))
(def $main ($ :#main))
(def $footer ($ :#footer))
(def $clear-completed ($ :#clear-completed))
(def ENTER 13)

(defn toggle-main-and-footer [n]
  (let [f (if (pos? n) j/show j/hide)]
    (f $main)
    (f $footer)))

(defn toggle-li-completed [$li]
  (fn [completed?]
     (let [f (if completed? j/add-class j/remove-class)]
       (f $li "completed"))))

(defpartial todo-li [id]
  [:li {:data-id id}
   [:div.view
    [:input.toggle {:type "checkbox"}]
    [:label]
    [:button.destroy]]
   [:input.edit]])

(defn enter? [e] (= ENTER (.-keyCode e)))

(defn new-todo [title]
  (model/new-todo title)
  (j/val $new-todo ""))

(defn bind-todo [todo $todo]
  (let [toggle-completed (-> ($ :.toggle $todo)
                             rxj/change
                             rxj/select-checked)
        destroy-click (-> ($ :.destroy $todo) rxj/click)]
    (rx/subscribe toggle-completed
                  #(model/mark-completed todo %))

    (rx/subscribe (rx/select (as-obs todo) #(:completed %))
                  (toggle-li-completed $todo))

    (rx/subscribe destroy-click #(model/remove-todo todo))
    (rx/subscribe destroy-click #(j/remove $todo))

    (j/inner ($ "label" $todo) (:title todo))
    (j/val ($ :.edit $todo) (:title todo))))

(defn add-todos [todos]
  (doseq [todo todos]
    (let [$html ($ (todo-li (:id todo)))]
      (bind-todo todo $html)
      (j/append $todo-list $html))))

(defn remove-todos [todos]
  (doseq [todo todos]
    (j/remove ($ (format "li[data-id=\"%s\"]" (:id todo))))))

(defn update-items-left [n]
  (j/inner $todo-count
           (format "<strong>%s</strong> %s left"
                   n (if (or (= 0 n) (> n 1))
                       "items"
                       "item"))))

(defn update-complete-count [n]
  (j/inner $clear-completed (format "Clear completed (%s)" n)))

(def todo-input-entered
  (-> $new-todo
      rxj/keyup
      (rx/where enter?)
      (rx/select #(j/val $new-todo))))

(def clear-completed-click
  (-> $clear-completed
      rxj/click))

(defn ^:export main []
  (rx/subscribe model/total-count toggle-main-and-footer)
  (rx/subscribe model/complete-count update-complete-count)
  (rx/subscribe model/incomplete-count update-items-left)
  (rx/subscribe model/todo-added add-todos)
  (rx/subscribe model/todo-removed remove-todos)

  (rx/subscribe todo-input-entered new-todo)
  (rx/subscribe clear-completed-click model/clear-completed))
