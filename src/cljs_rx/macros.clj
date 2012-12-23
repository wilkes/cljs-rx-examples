(ns cljs-rx.macros)

(defmacro defevent [event-name]
  (let [event-kw (keyword event-name)]
    `(do
       (defn ~event-name [elem# & [selector# event-data#]]
         (cljs-rx.jquery/on elem#
                            ~event-kw
                            selector#
                            event-data#)))))


(defmacro defwrap [name delegate & [mandatory-count optional-count]]
  (let [mandatory-args (vec (map (fn [_] (gensym))
                                 (range 0 (or mandatory-count 0))))
        var-args (vec (map (fn [_] (gensym))
                           (range 0 (or optional-count 0))))
        args mandatory-args
        args (if optional-count
               (concat args '[&] [var-args])
               args)]
    `(do
       (defn ~name [obs# ~@args]
         (~delegate obs# ~@mandatory-args ~@var-args)))))