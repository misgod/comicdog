(ns comicdog.common
  (:require [net.cgrand.enlive-html :as e]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [clojurewerkz.urly.core :refer [url-like host-of]])

  )


(defn extract-node [body selector]
  (let [snippet (e/html-snippet body)
        node (e/select snippet selector)]
    (apply str (e/emit* node))))


(defmulti extractor (fn [url] (-> url (url-like) (host-of))))
