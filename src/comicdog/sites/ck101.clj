(ns comicdog.sites.ck101
  (:require [net.cgrand.enlive-html :as e]
            [clojure.java.io :as io]
            [comicdog.common :refer :all]
            [clj-http.client :as client]))


(defn absolute-url [url]
  (cond
    (nil? url) nil
    (.startsWith url "http") url
    (.startsWith url "/") (str "http://comic.ck101.com" url)))


(defn extract-content-img [html]
  "extract image"
  (-> #"<img id = 'defualtPagePic' src=\"(.+?)\""
      (re-find  html)
      (second)))


(defn extract-content-next [html]
  "extract next page url"
  (-> #"<a href=\"(.*?)\" class=\"nextPageButton\""
      (re-find html)
      (second)
      (absolute-url)))

(defn extract-content-filename [html]
  "extract current page number of this epsoid and return filename to store this image"
  (let [selector [:div.pageNumber :span.pageOne :> e/text-node]
        to-int #(. Integer parseInt %)
        format-name #(format "%03d.jpg" %)]
    (-> html
        (extract-node selector)
        (to-int)
        (format-name))))

(defn extract-comic-name [html]
  "extract current comic name"
  (->> html
       (re-find #"<title>(.+?) [-|\/] .*<\/title>")
       (second)
       (.trim)))


(defn extract-comic-lists [html]
  "extract all epsoids of this comic"
  (->> (extract-node html [:div.comicBox :div.relativeRec :h3.recTitle :a])
       (re-seq #"href=\"(/vols.*?)\"")
       (map second)
       (map #(str "http://comic.ck101.com" %))
       (reverse)))

;; (defn extract-epsoid-entry [html]
;;   "extract the entry url of this epsoid"
;;   )


(defn extract-epsoid-name [html]
  "extract the name of this epsoid for dir create"
  (-> html
      (extract-node [:h2.pageTitle :strong e/text-node])
      (.trim)))



(def ck101-extractor
  {:comic-name  extract-comic-name
   :epsoid-lists extract-comic-lists
   :epsoid-name extract-epsoid-name
   :content-filename extract-content-filename
   :content-image extract-content-img
   :content-next extract-content-next})



(defmethod extractor "comic.ck101.com" [url] ck101-extractor)
