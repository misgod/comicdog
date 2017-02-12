(ns comicdog.sites.cartoonmad
  (:require [net.cgrand.enlive-html :as e]
            [clojure.java.io :as io]
            [comicdog.common :refer :all]
            [clj-http.client :as client]))


(defn absolute-url [url]
  (cond
    (nil? url) nil
    (.startsWith url "http") url
    (.startsWith url "/") (str "http://www.cartoonmad.com" url)
    :else (str  "http://www.cartonmad.com/comic/" url)))


(defn extract-content-img [html]
  "extract image"
  (-> #"<img src=\"(http:\/\/\w+.cartoonmad.com\/.*?\.jpg)\""
      (re-find  html)
      (second)))


(defn extract-content-next [html]
  "extract next page url"
  (-> #"<a class=pages href=(.*\.html?)><img src=/image/rad\.gif"
      (re-find html)
      (second)
      (absolute-url)))

(defn extract-content-filename [html]
  "extract current page number of this episode and return filename to store this image"
  (let [selector [:div.pageNumber :span.pageOne :> e/text-node]
        to-int #(. Integer parseInt %)
        format-name #(format "%03d.jpg" %)]
    (->> html
        (re-find  #"<option selected>第 (\d+)? 頁</option>")
        (second)
        (to-int)
        (format-name))))



(defn extract-comic-name [html]
  "extract current comic name"
  (->> html
       (re-find #"<title>(.+?) [-|\/] .*<\/title>")
       (second)
       (.trim)))


(defn extract-comic-lists [html]
  "extract all episodes of this comic"
  (->> html
       (re-seq #"<td>•<a href=(\/comic\/.*?html)")
       (map second)
       (map absolute-url)))

(defn extract-episode-name [html]
  "extract the name of this episode for dir create"
  (->> html
       (re-find #"<title>.*? [-|\/] (.+?) [-|\/] .*<\/title>")
      (second)
      (.trim)
      (filter #(not= \space %))
      (apply str)))


(def cartoonmad-extractor
  {:encoding "BIG5"
   :comic-name  extract-comic-name
   :episode-lists extract-comic-lists
   :episode-name extract-episode-name
   :content-filename extract-content-filename
   :content-image extract-content-img
   :content-next extract-content-next})



(defmethod extractor "www.cartoonmad.com" [url] cartoonmad-extractor)

