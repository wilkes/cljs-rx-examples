(ns cljs-rx.macros)

(defmacro defevent [event-name]
  (let [event-kw (keyword event-name)]
    `(do
       (defn ~event-name [elem# & [selector# event-data#]]
         (cljs-rx.jquery/on elem#
                            ~event-kw
                            selector#
                            event-data#)))))


(defmacro defwrap [name delegate & [mandatory optional]]
  (let [mandatory-args (vec (map gensym mandatory))
        var-args (if (symbol? optional)
                   [(gensym optional)]
                   (vec (map gensym optional)))
        args mandatory-args
        args (if optional
               (concat args '[&] (if (symbol? optional)
                                   var-args
                                   [var-args]))
               args)
        obs (gensym "obs")]

    `(defn ~name [~obs ~@args]
       (~delegate ~obs ~@mandatory-args ~@var-args))))