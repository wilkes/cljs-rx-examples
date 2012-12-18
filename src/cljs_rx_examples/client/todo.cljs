(ns cljs-rx-examples.client.todo
  (:require [cljs-rx.jquery :as rxj]
            [cljs-rx.observable :as rx]
            [cljs-rx.clojure :as rxclj]
            [clojure.data :refer [diff]]
            [crate.core :as crate]
            [jayq.core :refer [$] :as j]
            [jayq.util :refer [log]])
  (:use-macros [crate.def-macros :only [defpartial]]))

(def log-pr (comp log pr-str))

(def next-id (atom 0))

(defn make-todo [title]
  (rxclj/observable-map
   {:id (swap! next-id inc)
    :title title
    :completed false}))

(defn mark-completed [todo]
  (assoc todo :completed true))

(def todos (atom []))
(def todos-obs (rxclj/observable-atom todos))

(defpartial todo-li []
  [:li
   [:div.view
    [:input.toggle {:type "checkbox"}]
    [:label]
    [:button.destroy]]
   [:input.edit]])

(def $todo-list ($ :#todo-list))
(def $new-todo ($ :#new-todo))
(def $todo-count ($ :#todo-count))

(def ENTER 13)
(defn enter? [e]
  (= ENTER (.-keyCode e)))

(defn populate-todo-li [$todo {:keys [id title completed]}]
  (j/data $todo :id id)
  (when completed
    (j/add-class $todo :completed)
    (.prop ($ :.toggle $todo) "checked" true))
  (j/inner ($ "label" $todo) title)
  (j/val ($ :.edit $todo) title)
  $todo)

(defn todo-count-html [n]
  (let [items (if (or (= 0 n) (> n 1))
                "items"
                "item")]
    (format "<strong>%s</strong> %s left" n items)))

(defn update-count [n]
  (j/inner $todo-count (todo-count-html n)))

(defn changed [[in-a in-b in-both]]
  (log "in-a:" (pr-str in-a))
  (log "in-b:" (pr-str in-b))
  (log "in-both:" (pr-str in-both)))

(defn main []
  (let [init-todos [(-> (make-todo "Create a TodoMVC template")
                        mark-completed)
                    (make-todo "Rule the web")]
        new-todo (-> $new-todo
                     rxj/keyup
                     (rx/where enter?)
                     (rx/select #(j/val $new-todo)))
        todos-diff (-> todos-obs
                       (rx/buffer-with-count 2 1)
                       (rx/select #(diff (first %)
                                         (second %)))
                       (.startWith []))
        completed-count (-> todos-obs
                            (rx/select #(count (remove :completed %))))]
    (rx/subscribe todos-diff changed)
    (rx/subscribe completed-count update-count)
    (rx/subscribe new-todo #(do
                              (swap! todos conj (make-todo %))
                              (j/val $new-todo "")))
    (doseq [todo init-todos]
      (swap! todos conj todo)
      (rxclj/subscribe todo log-pr)
      (let [li (populate-todo-li ($ (todo-li)) todo)]
        (j/append $todo-list li))
      (assoc todo :random (gensym)))))
