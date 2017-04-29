To use:
* docker pull busybox:latest
* ./gradlew build
* Repro issue with spotify library: java -cp build/libs/*.jar spotify.Testing
* No repro with same command just using TCP instead of unix socket: java -cp build/libs/*.jar spotifytcp.Testing
* Using docker-java library (no repro): java -cp build/libs/*.jar nonspotify.Testing
* Workaround with spotify library: -cp build/libs/*.jar spotifyworkaround.Testing