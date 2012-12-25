(ns cljs-rx-examples.client.todo.app
  (:require [cljs-rx-examples.client.todo.model :as model]
            [cljs-rx.jquery :as rxj]
            [cljs-rx.dom :as rxdom]
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

(defn bind-todo [todo $todo]
  (rxdom/checked? ($ :.toggle $todo)
                  #(model/mark-completed todo %))
  (rxclj/bind-attr todo :completed
                   (toggle-li-completed $todo))
  (rxdom/clicked? ($ :.destroy $todo)
                  #(model/remove-todo todo))
  (rxdom/bind-inner ($ "label" $todo)
                    (rxclj/select-key todo :title))
  (rxclj/bind-attr todo :title
                   #(j/val ($ :.edit $todo) (:title todo)))
  (rxdom/dblclicked? ($ "label" $todo)
                     #(do (j/add-class $todo "editing")
                          (.focus ($ :.edit $todo))))
  (rxdom/input-entered ($ :.edit $todo)
                       #(do (model/edit-title todo %)
                            (j/remove-class $todo "editing")))

  (-> todo rxclj/subject (.onNext todo)))

(defn add-todos [todos]
  (doseq [todo todos]
    (let [$html ($ (todo-li (:id todo)))]
      (bind-todo todo $html)
      (j/append $todo-list $html))))

(defn items-left-count [n]
  (format "<strong>%s</strong> %s left"
          n (if (= 0 1) "item" "items")))

(defn set-selected-view [{:keys [token]}]
  (-> ($ "#filters a")
      (j/remove-class :selected))
  (-> ($ (format "#filters a[href=\"#%s\"]" token))
      (j/add-class :selected)))

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
  (rxdom/show $main model/total-count pos?)
  (rxdom/show $footer model/total-count pos?)
  (rxdom/bind-inner $todo-count model/incomplete-count items-left-count)
  (rxdom/bind-remove model/todo-removed #(format "[data-id=\"%s\"]" (:id %)))

  (rxdom/show $clear-completed model/complete-count pos?)
  (rxdom/bind-inner $clear-completed model/complete-count
              #(format "Clear completed (%s)" %))

  (rxdom/checked $toggle-all model/all-completed)

  (rxdom/input-entered $new-todo (fn [x]
                             (model/new-todo x)
                             (j/val $new-todo "")))


  (rx/subscribe model/todo-added add-todos)

  (rxdom/clicked? $clear-completed model/clear-completed)
  (rxdom/checked? $toggle-all model/toggle-completed)

  (rx/subscribe history-observable filter-list-view)
  (rx/subscribe history-observable set-selected-view))
