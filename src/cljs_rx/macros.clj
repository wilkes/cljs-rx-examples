(ns cljs-rx.macros)

(defmacro defevent [event-name]
  (let [event-kw (keyword event-name)]
    `(do
       (defn ~event-name [elem# & [selector# event-data#]]
         (cljs-rx.jquery/on elem#
                            ~event-kw
                            selector#
                            event-data#)))))