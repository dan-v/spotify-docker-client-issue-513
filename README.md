To use:
* docker pull busybox:latest
* ./gradlew build
* Repro issue with spotify library: java -cp build/libs/*.jar spotify.Testing
* Workaround with spotify library: -cp build/libs/*.jar spotifyfix.Testing
* Using docker-java library (no repro): java -cp build/libs/*.jar nonspotify.Testing

## Curl repro (not as reliable)
```
echo "spotify library in curl version.."
containerId=$(docker run -d busybox:latest sleep 99999999)
while [ 1 ]; do
  echo "Calling GET for ${containerId}"
  curl -s -o /dev/null -w "Response: %{http_code}\n" --unix-socket /var/run/docker.sock "http:/containers/${containerId}/json"
  echo "Creating container exec for ${containerId}"
  uuid=$(curl -s -H "Content-Type: application/json" --data '{"AttachStderr":true,"AttachStdout":true,"Cmd":["/bin/sh","-c","sleep 5 && echo done"]}' --request POST --unix-socket /var/run/docker.sock "http:/containers/${containerId}/exec" | jq -r .Id)
  echo "Starting container exec ${uuid}"
  curl -H "Content-Type: application/json" --data '{}' --request POST --unix-socket /var/run/docker.sock "http:/exec/${uuid}/start"
done
```

## Commands as seen from docker daemon in debug mode
* spotify.Testing
```
Apr 28 14:38:46 localhost dockerd: time="2017-04-28T14:38:46.701798243-07:00" level=debug msg="Calling GET /exec/45b365a62ef66e0b991dcdcef911085e34d60ab7dabc75282c1b88b754f17b7f/json"
Apr 28 14:38:46 localhost dockerd: time="2017-04-28T14:38:46.704656346-07:00" level=debug msg="Calling GET /containers/83a4642cdaf7094377e64787a44afd1b1d3aba6cfb8e795796a52ec62c7f8273/json"
Apr 28 14:38:46 localhost dockerd: time="2017-04-28T14:38:46.710976473-07:00" level=debug msg="Calling POST /containers/83a4642cdaf7094377e64787a44afd1b1d3aba6cfb8e795796a52ec62c7f8273/exec"
Apr 28 14:38:46 localhost dockerd: time="2017-04-28T14:38:46.711154220-07:00" level=debug msg="form data: {\"AttachStderr\":true,\"AttachStdout\":true,\"Cmd\":[\"sleep\",\"5\"]}"
Apr 28 14:38:46 localhost dockerd: time="2017-04-28T14:38:46.713413598-07:00" level=debug msg="Calling POST /exec/8cc2707b8435576aacac0efe7cc618213a53068ce1bf3661813105e174818d21/start"
Apr 28 14:38:46 localhost dockerd: time="2017-04-28T14:38:46.713487037-07:00" level=debug msg="form data: {}"
Apr 28 14:38:46 localhost dockerd: time="2017-04-28T14:38:46.713549853-07:00" level=debug msg="starting exec command 8cc2707b8435576aacac0efe7cc618213a53068ce1bf3661813105e174818d21 in container 83a4642cdaf7094377e64787a44afd1b1d3aba6cfb8e795796a52ec62c7f8273"
Apr 28 14:38:46 localhost dockerd: time="2017-04-28T14:38:46.713617648-07:00" level=debug msg="attach: stdout: begin"
Apr 28 14:38:46 localhost dockerd: time="2017-04-28T14:38:46.713693591-07:00" level=debug msg="attach: stderr: begin"
Apr 28 14:38:46 localhost dockerd: time="2017-04-28T14:38:46.777806000-07:00" level=debug msg="libcontainerd: received containerd event: &types.Event{Type:\"start-process\", Id:\"83a4642cdaf7094377e64787a44afd1b1d3aba6cfb8e795796a52ec62c7f8273\", Status:0x0, Pid:\"8cc2707b8435576aacac0efe7cc618213a53068ce1bf3661813105e174818d21\", Timestamp:(*timestamp.Timestamp)(0xc420f49b30)}"
```

* nonspotify.Testing
```
Apr 28 14:40:21 localhost dockerd: time="2017-04-28T14:40:21.895494978-07:00" level=debug msg="Calling GET /exec/520176ee47e0677e43a472b3d0aa06558cde6211396251197da4e0800bab8531/json"
Apr 28 14:40:21 localhost dockerd: time="2017-04-28T14:40:21.897834671-07:00" level=debug msg="Calling POST /containers/a2c5ff9dd251cb0adfdf265f0b80b2485a3a8ac86ebf8074670215a4231baf36/exec"
Apr 28 14:40:21 localhost dockerd: time="2017-04-28T14:40:21.897923688-07:00" level=debug msg="form data: {\"AttachStderr\":true,\"AttachStdout\":true,\"Cmd\":[\"sleep\",\"5\"],\"containerId\":\"a2c5ff9dd251cb0adfdf265f0b80b2485a3a8ac86ebf8074670215a4231baf36\"}"
Apr 28 14:40:21 localhost dockerd: time="2017-04-28T14:40:21.901574097-07:00" level=debug msg="Calling POST /exec/0a684aece85903590602ec25b5e7111f51054679c959e752c2da33380053b20a/start"
Apr 28 14:40:21 localhost dockerd: time="2017-04-28T14:40:21.901634284-07:00" level=debug msg="form data: {\"Detach\":false}"
Apr 28 14:40:21 localhost dockerd: time="2017-04-28T14:40:21.901710124-07:00" level=debug msg="starting exec command 0a684aece85903590602ec25b5e7111f51054679c959e752c2da33380053b20a in container a2c5ff9dd251cb0adfdf265f0b80b2485a3a8ac86ebf8074670215a4231baf36"
Apr 28 14:40:21 localhost dockerd: time="2017-04-28T14:40:21.901759532-07:00" level=debug msg="attach: stdout: begin"
Apr 28 14:40:21 localhost dockerd: time="2017-04-28T14:40:21.901771868-07:00" level=debug msg="attach: stderr: begin"
Apr 28 14:40:21 localhost dockerd: time="2017-04-28T14:40:21.965883218-07:00" level=debug msg="libcontainerd: received containerd event: &types.Event{Type:\"start-process\", Id:\"a2c5ff9dd251cb0adfdf265f0b80b2485a3a8ac86ebf8074670215a4231baf36\", Status:0x0, Pid:\"0a684aece85903590602ec25b5e7111f51054679c959e752c2da33380053b20a\", Timestamp:(*timestamp.Timestamp)(0xc42155ee90)}"
Apr 28 14:40:21 localhost dockerd: time="2017-04-28T14:40:21.965957457-07:00" level=debug msg="libcontainerd: event unhandled: type:\"start-process\" id:\"a2c5ff9dd251cb0adfdf265f0b80b2485a3a8ac86ebf8074670215a4231baf36\" pid:\"0a684aece85903590602ec25b5e7111f51054679c959e752c2da33380053b20a\" timestamp:<seconds:1493415621 nanos:965592193 > "

```