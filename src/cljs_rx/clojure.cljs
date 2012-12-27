(ns cljs-rx.clojure
  (:require [cljs-rx.observable :as rx]
            [cljs-rx.subject :as subject]
            [clojure.data :as data]
            [clojure.set :as set]
            [jayq.util :refer [log]]))

(defprotocol IToObservable
  (-to-observable [this]))

(defprotocol IToSubject
  (-to-subject [this]))

(defprotocol ISubjectCollection
  (-remove [this x]))

(deftype ObservableMap [state meta subject]
  ISubjectCollection
  (-remove [this x]
    (reset! this
            (into {} (remove (partial = x) state))))

  IToObservable
  (-to-observable [this]
    (.asObservable subject))

  IToSubject
  (-to-subject [this]
    subject)

  IDeref
  (-deref [_] state)

  IWatchable
  (-notify-watches [this oldval newval]
    (.onNext subject newval))
  (-add-watch [this key f]
    (rx/subscribe subject f))
  (-remove-watch [this key])

  IPrintWithWriter
  (-pr-writer [this writer opts]
    (-write writer "#<ObservableMap: ")
    (-pr-writer state writer opts)
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
    (reset! this cljs.core.PersistentArrayMap/EMPTY))

  IEquiv
  (-equiv [_ other] (equiv-map state (.-state other)))

  IHash
  (-hash [this] (-hash state))

  ISeqable
  (-seq [_] (-seq state))

  ICounted
  (-count [_] (-count state))

  ILookup
  (-lookup [coll k]
    (-lookup coll k nil))

  (-lookup [_ k not-found]
    (-lookup state k not-found))

  IKVReduce
  (-kv-reduce [_ f init]
    (-kv-reduce state f init))

  IFn
  (-invoke [coll k]
    (-lookup coll k))

  (-invoke [coll k not-found]
    (-lookup coll k not-found)))

(deftype ObservableVector [state meta subject]
  ISubjectCollection
  (-remove [this x]
    (reset! this
              (into [] (remove (partial = x) state))))

  IToObservable
  (-to-observable [this]
    (.asObservable subject))

  IToSubject
  (-to-subject [this]
    subject)

  IDeref
  (-deref [_] state)

  IWatchable
  (-notify-watches [this oldval newval]
    (.onNext subject newval))
  (-add-watch [this key f]
    (rx/subscribe subject f))
  (-remove-watch [this key])


  IPrintWithWriter
  (-pr-writer [this writer opts]
    (-write writer "#<ObservableVector: ")
    (-pr-writer state writer opts)
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
    (reset! this cljs.core.PersistentVector/EMPTY))

  ISequential
  IEquiv
  (-equiv [_ other] (equiv-sequential state (.-state other)))

  IHash
  (-hash [this] (hash state))

  ISeqable
  (-seq [_]
    (-seq state))

  ICounted
  (-count [_] (-count state))

  IIndexed
  (-nth [_ n]
    (-nth state n))
  (-nth [_ n not-found]
    (-nth state n not-found))

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
    (-reduce state f))
  (-reduce [_ f start]
    (-reduce state f start))

  IKVReduce
  (-kv-reduce [_ f init]
    (-kv-reduce state f init))

  IFn
  (-invoke [coll k]
    (-lookup coll k))
  (-invoke [coll k not-found]
    (-lookup coll k not-found))

  IReversible
  (-rseq [coll]
    (-rseq state)))

(extend-protocol IToObservable
  js/Rx.Observable
  (-to-observable [x]
    (.asObservable x)))

(defn observable-map [m]
  (ObservableMap. m nil (subject/behavior)))

(defn observable-vector [v]
  (ObservableVector. v nil (subject/behavior)))

(defn to-observable [obs]
  (-to-observable obs))

(defn to-subject [obs]
  (-to-subject obs))

(defn- subscribe-parent [parent child]
  (if (and (satisfies? IToSubject parent)
           (satisfies? IToObservable child))
    (rx/subscribe child
                  (fn [x]
                    (.onNext (to-subject parent) @parent))))
  parent)

(defn add! [obs x]
  (swap! obs conj x)
  (subscribe-parent obs x))

(defn remove! [obs x]
  (-remove obs x))

(defn obs-assoc! [obs k v]
  (swap! obs assoc k v)
  (subscribe-parent obs v))

(defn obs-dissoc! [obs k]
  (swap! obs dissoc k))

(defn select-key [obs k]
  (rx/select obs #(get % k)))

(defn bind-attr [target key f]
  (rx/subscribe (select-key target key) f))

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
