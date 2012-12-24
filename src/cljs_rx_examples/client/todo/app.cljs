(ns cljs-rx-examples.client.todo.app
  (:require [cljs-rx-examples.client.todo.model :as model]
            [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [cljs-rx.history :refer [history-observable]]
            [cljs-rx.clojure :refer [observable] :as rxclj]
            [clojure.set :as set]
            [crate.core :as crate]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(def log-pr (comp log pr-str))

(def $todo-list ($ :#todo-list))
(def $new-todo ($ :#new-todo))
(def $todo-count ($ :#todo-count))
(def $toggle-all ($ :#toggle-all))
(def $main ($ :#main))
(def $footer ($ :#footer))
(def $clear-completed ($ :#clear-completed))
(def ENTER 13)

(defn enter? [e] (= ENTER (.-keyCode e)))

(defn input-entered [$input]
  (-> $input
      rxj/keyup
      (rx/where enter?)
      (rx/select #(j/val $input))))

(defn toggle-main-and-footer [n]
  (let [f (if (pos? n) j/show j/hide)]
    (f $main)
    (f $footer)))

(defn toggle-li-completed [$li]
  (fn [completed?]
     (let [f (if completed? j/add-class j/remove-class)]
       (f $li "completed")
       (j/attr ($ :.toggle $li) :checked completed?))))

(defpartial todo-li [id]
  [:li {:data-id id}
   [:div.view
    [:input.toggle {:type "checkbox"}]
    [:label]
    [:button.destroy]]
   [:input.edit]])

(defn new-todo [title]
  (model/new-todo title)
  (j/val $new-todo ""))

(defn bind-todo [todo $todo]
  (let [toggle-completed (-> ($ :.toggle $todo)
                             rxj/change
                             rxj/select-checked)
        destroy-click (-> ($ :.destroy $todo) rxj/click)
        edit-click (-> ($ "label" $todo) rxj/dblclick)
        edit-return (input-entered ($ :.edit $todo))
        completed-obs (rxclj/select-key todo :completed)
        title-obs (rxclj/select-key todo :title)]

    (rx/subscribe toggle-completed #(model/mark-completed todo %))

    (rx/subscribe completed-obs (toggle-li-completed $todo))

    (rx/subscribe destroy-click #(model/remove-todo todo))
    (rx/subscribe destroy-click #(j/remove $todo))

    (rx/subscribe title-obs  #(j/inner ($ "label" $todo) %))

    (rx/subscribe title-obs #(j/val ($ :.edit $todo) (:title todo)))

    (rx/subscribe edit-click #(do
                                (j/add-class $todo "editing")
                                (.focus ($ :.edit $todo))))

    (rx/subscribe edit-return #(model/edit-title todo %))
    (rx/subscribe edit-return #(j/remove-class $todo "editing"))


    (-> todo rxclj/subject (.onNext todo))))

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
  (if (pos? n)
    (-> $clear-completed
        (j/inner (format "Clear completed (%s)" n))
        j/show)
    (j/hide $clear-completed)))

(defn update-all-completed [all-completed?]
  (j/attr $toggle-all :checked  all-completed?))

(def todo-input-entered (input-entered $new-todo))

(def clear-completed-click
  (-> $clear-completed
      rxj/click))

(def toggle-all-click
  (-> $toggle-all
      rxj/click
      (rx/select #(boolean (j/attr $toggle-all :checked)))))

(defn show-active []
  (j/hide ($ "li.completed" $todo-list))
  (j/show ($ "li[class!=completed]" $todo-list)))

(defn show-completed []
  (j/hide ($ "li[class!=completed]" $todo-list))
  (j/show ($ "li.completed" $todo-list)))

(defn show-all []
  (j/show ($ "li" $todo-list)))

(defn filter-list-view [{:keys [token]}]
  (case token
    "/active" (show-active)
    "/completed" (show-completed)
    (show-all)))

(defn ^:export main []
  (rx/subscribe model/total-count toggle-main-and-footer)
  (rx/subscribe model/complete-count update-complete-count)
  (rx/subscribe model/incomplete-count update-items-left)
  (rx/subscribe model/todo-added add-todos)
  (rx/subscribe model/todo-removed remove-todos)
  (rx/subscribe model/all-completed update-all-completed)

  (rx/subscribe todo-input-entered new-todo)
  (rx/subscribe clear-completed-click model/clear-completed)
  (rx/subscribe toggle-all-click model/toggle-completed)
  (rx/subscribe history-observable filter-list-view))
