(ns comicdog.core
  (:require [net.cgrand.enlive-html :as e]
            [clojure.java.io :as io]
            [clj-http.client :as client]
            [comicdog.common :refer :all]
            [comicdog.sites.ck101 :refer :all]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class :main true))


(declare ^:dynamic *extractor*)


(defn mkdir
  ([name] (doto (io/file name)
            (.mkdir)))
  ([name parent] (-> (io/file parent  name)
                     (mkdir))))

(defn get-html [url]
  (let [response (client/get url)
        status (:status response)
        body   (:body response)]
    (if (or (= status 200) (= status 201))
      body
      (prn "download" url "fail"))))

(defn download-image [uri file]
  (try
    (with-open [in (io/input-stream uri)
                out (io/output-stream file)]
      (io/copy in out))
    (prn " Done!")
    (catch Exception e (prn  "error!\n " (.getMessage e)))))



(defn download-content [url dir]
  "download image by pages"
  (let [html (get-html url)
        filename ((:content-filename *extractor*) html)
        next ((:content-next *extractor*) html)
        imgurl ((:content-image *extractor*) html)
        file (io/file dir filename)]
    (print "download " filename " => ")

    (if (.exists file)
      (prn "exists!")
      (download-image imgurl file))

    (when-not (nil? next)
      (recur next dir))))



(defn download-episode [url dir]
  "Make dir and download episode"
  (let [html (get-html url)
        name ((:epsoid-name *extractor*) html)
        ;;entry ((:epsoid-entry *extractor*) html)
        epsoid-dir (mkdir name dir)]
    (prn "Download episode:" name)
    (download-content url epsoid-dir)))


(defn download-all [url]
  "Download all episodes of this comic"
  (let [html (get-html url)
        name ((:comic-name *extractor*) html)
        dir  (mkdir name)]
    (doseq [epsode-url ((:epsoid-lists *extractor*) html)]
      (download-episode epsode-url dir))))

(defn go [url]
  (binding [*extractor* (extractor url)]
    (download-all url)))


(defn -main [& args]
  (let [url (first args)]
    (if (nil? url)
      (prn "no url")
      (go url))))
