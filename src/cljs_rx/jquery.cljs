(ns cljs-rx.jquery
  (:require  [cljs-rx.observable :as rx]
             [jayq.core :refer [$] :as j]
             [jayq.util :refer [clj->js log]])
  (:use-macros [cljs-rx.macros :only [defevent]]))

(def log-pr (comp log pr-str))

(defn on [$elem events & [selector data]]
  (.onAsObservable $elem
                   (j/->event events)
                   (j/->selector selector)
                   data))

(defevent change)
(defevent click)
(defevent dblclick)
(defevent focus)
(defevent focusin)
(defevent focusout)
(defevent hover)
(defevent keydown)
(defevent keypress)
(defevent keyup)
(defevent load)
(defevent mousedown)
(defevent mouseenter)
(defevent mouseleave)
(defevent mousemove)
(defevent mouseout)
(defevent mouseover)
(defevent mouseup)
(defevent ready)
(defevent resize)
(defevent scroll)
(defevent select)
(defevent submit)
(defevent unload)

(defn ajax
  ([url settings]
     (ajax (assoc settings :url url)))
  ([settings]
     (let [settings (j/->ajax-settings settings)]
       (-> (.ajaxAsObservable js/jQuery settings)
           (rx/select (fn [val] (js->clj val :keywordize-keys true)))))))