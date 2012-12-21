(ns cljs-rx.clojure
  (:require [cljs-rx.observable :as rx]
            [cljs-rx.subject :as subject]
            [clojure.data :as data]
            [clojure.set :as set]
            [jayq.util :refer [log]]))

(defn observable-atom [atm]
  (let [k (gensym)
        subject (subject/behavior)]
    (add-watch atm k
               (fn [_ _ _ new-state]
                 (.onNext subject new-state)))
    subject))

(defprotocol IBehavior
  (-remove [this x])
  (-replace [this x])
  (-observable [this])
  (-subject [this]))

(deftype ObservableMap [meta v behavior]
  IBehavior
  (-replace [this new-v]
    (set! (.-v this) new-v)
    (.onNext behavior new-v)
    this)

  (-remove [this x]
    (-replace this
              (into {} (remove (partial = x) v))))

  (-observable [this]
    (.asObservable behavior))

  (-subject [this]
    behavior)

  IPrintWithWriter
  (-pr-writer [this writer opts]
    (-write writer "#<ObservableMap: ")
    (-pr-writer v writer opts)
    (-write writer ">"))

  Object
  (toString [this]
    (pr-str this))

  IWithMeta
  (-with-meta [this meta]
    (set! (.-meta this) meta)
    this)

  IMeta
  (-meta [_] meta)

  IEmptyableCollection
  (-empty [this]
    (-replace this cljs.core.PersistentArrayMap/EMPTY))

  IEquiv
  (-equiv [_ other] (equiv-map v (.-v other)))

  IHash
  (-hash [this] (-hash v))

  ISeqable
  (-seq [_] (-seq v))

  ICounted
  (-count [_] (-count v))

  ILookup
  (-lookup [coll k]
    (-lookup coll k nil))

  (-lookup [_ k not-found]
    (-lookup v k not-found))

  IKVReduce
  (-kv-reduce [_ f init]
    (-kv-reduce v f init))

  IFn
  (-invoke [coll k]
    (-lookup coll k))

  (-invoke [coll k not-found]
    (-lookup coll k not-found)))

(defn observable-map [m]
  (ObservableMap. nil m (subject/behavior)))

(deftype ObservableVector [meta v behavior]
  IBehavior
  (-replace [this new-v]
    (set! (.-v this) new-v)
    (.onNext behavior new-v)
    this)

  (-remove [this x]
    (-replace this
              (into [] (remove (partial = x) v))))

  (-observable [this]
    (.asObservable behavior))

  (-subject [this]
    behavior)

  IPrintWithWriter
  (-pr-writer [this writer opts]
    (-write writer "#<ObservableVector: ")
    (-pr-writer v writer opts)
    (-write writer ">"))

  Object
  (toString [this]
    (pr-str this))

  IWithMeta
  (-with-meta [this meta]
    (set! (.-meta this) meta)
    this)

  IMeta
  (-meta [_] meta)

  IEmptyableCollection
  (-empty [this]
    (-replace this cljs.core.PersistentVector/EMPTY))

  ISequential
  IEquiv
  (-equiv [_ other] (equiv-sequential v (.-v other)))

  IHash
  (-hash [this] v)

  ISeqable
  (-seq [_]
    (-seq v))

  ICounted
  (-count [_] (-count v))

  IIndexed
  (-nth [_ n]
    (-nth v n))
  (-nth [_ n not-found]
    (-nth v n not-found))

  ILookup
  (-lookup [coll k] (-nth coll k nil))
  (-lookup [coll k not-found] (-nth coll k not-found))

  IMapEntry
  (-key [coll]
    (-nth coll 0))
  (-val [coll]
    (-nth coll 1))

  IReduce
  (-reduce [_ f]
    (-reduce v f))
  (-reduce [_ f start]
    (-reduce v f start))

  IKVReduce
  (-kv-reduce [_ f init]
    (-kv-reduce v f init))

  IFn
  (-invoke [coll k]
    (-lookup coll k))
  (-invoke [coll k not-found]
    (-lookup coll k not-found))

  IReversible
  (-rseq [coll]
    (-rseq v)))


(defn observable-vector [v]
  (ObservableVector. nil v (subject/behavior)))

(defn observable [obs]
  (-observable obs))

(defn subject [obs]
  (-subject obs))

(defn- subscribe-parent [parent child]
  (if (and (satisfies? IBehavior parent)
           (satisfies? IBehavior child))
    (rx/subscribe (observable child)
                  (fn [x]
                    (.onNext (subject parent) (.-v parent)))))
  parent)

(defn update!
  ([obs f]
     (-replace obs (f (.-v obs))))
  ([obs f x]
     (-replace obs (f (.-v obs) x)))
  ([obs f x y]
     (-replace obs (f (.-v obs) x y)))
  ([obs f x y z]
     (-replace obs (f (.-v obs) x y z)))
  ([obs f x y z & more]
     (-replace obs (apply f (.-v obs) x y z more))))

(defn add! [obs x]
  (update! obs conj x)
  (subscribe-parent obs x))

(defn remove! [obs x]
  (-remove obs x))

(defn obs-assoc! [obs k v]
  (update! obs assoc k v)
  (subscribe-parent obs v))

(defn obs-dissoc! [obs k]
  (update! obs dissoc k))

(defn select-key [obs k]
  (rx/select (observable obs) #(get % k)))

(defn diff [obs]
  (-> obs
      (rx/buffer-with-count 2 1)
      (rx/select (fn [buffer]
                   (let [old (first buffer)
                         new (second buffer)]
                     [(vec (remove #(some #{%} old) new))
                      (vec (remove #(some #{%} new) old))])))
      rx/distinct-until-changed))

(defn added [obs]
  (-> obs
      (rx/select first)
      (rx/where (comp not empty?))))

(defn removed [obs]
  (-> obs
      (rx/select second)
      (rx/where (comp not empty?))))
