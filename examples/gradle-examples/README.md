# kubernetes-java-annotations Example
####Kubernetes java annotations Example with Spring Boot and Gradle

#####Step 1:
run gradlew build

#####Step 2:
Check the generated Kubernetes deployment scripts under:
./build/classes/java/main/META-INF/ap4k

E.g:./build/classes/java/main/META-INF/ap4k/kubernetes.yml

#####Step 3:
Run the kubernetes.yml into Kubernetes, or use the script:
deployInKubernetes.bat, 
Or just run the Kubectl command:
kubectl apply -f ./build/classes/java/main/META-INF/ap4k/kubernetes.yml

Thank you !
#####Hi, my name is Alexandru Lungu and I will like to send some thanks to Iocanel, the author of the kubernetes annotations plugin.
(https://github.com/ap4k/ap4k/commits?author=iocanel)
#####Official extension page:
https://github.com/ap4k/ap4k
