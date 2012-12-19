(ns cljs-rx-examples.client.todo.app
  (:require [cljs-rx-examples.client.todo.model :as model]
            [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [cljs-rx.clojure :refer [as-obs] :as rxclj]
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
(def ENTER 13)

(defn toggle-main-and-footer [n]
  (let [f (if (pos? n) j/show j/hide)]
    (f $main)
    (f $footer)))

(defn toggle-li-completed [$li]
  (fn [completed?]
     (let [f (if completed? j/add-class j/remove-class)]
       (f $li "completed"))))

(defpartial todo-li []
  [:li
   [:div.view
    [:input.toggle {:type "checkbox"}]
    [:label]
    [:button.destroy]]
   [:input.edit]])

(defn enter? [e] (= ENTER (.-keyCode e)))

(defn bind-todo [todo $todo]
  (let [toggle-completed (-> ($ :.toggle $todo)
                             rxj/change
                             rxj/select-checked)]
    (j/data $todo :id (:id todo))

    (rx/subscribe toggle-completed
                  #(model/mark-completed todo %))

    (rx/subscribe (rx/select (as-obs todo) #(:completed %))
                  (toggle-li-completed $todo))

    (j/inner ($ "label" $todo) (:title todo))
    (j/val ($ :.edit $todo) (:title todo))))

(def new-todo-obs
  (-> $new-todo
      rxj/keyup
      (rx/where enter?)
      (rx/select #(j/val $new-todo))))

(defn update-items-left [n]
  (j/inner $todo-count
           (format "<strong>%s</strong> %s left"
                   n (if (or (= 0 n) (> n 1))
                       "items"
                       "item"))))

(defn new-todo [title]
  (let [todo (model/new-todo title)
        $html ($ (todo-li))]
    (bind-todo todo $html)
    (j/append $todo-list $html)
    (j/val $new-todo "")))

(defn main []
  (rx/subscribe model/total-count toggle-main-and-footer)
  (rx/subscribe model/incomplete-count update-items-left)
  (rx/subscribe new-todo-obs new-todo))
