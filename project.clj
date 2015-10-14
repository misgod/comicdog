(defproject comicdog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.16"]
                 [clj-http "2.0.0"]
                 [enlive "1.1.5"]
                 [org.clojure/tools.cli "0.3.1"]]
  :plugins [[cider/cider-nrepl "0.9.1"]]
  :main comicdog.core
  )
