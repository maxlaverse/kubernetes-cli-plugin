CHANGELOG
=========

1.1.0
-----
* Upgrade kubernetes-credentials to 0.3.1 [#1](https://github.com/jenkinsci/kubernetes-cli-plugin/pull/1)
* Add support for FileCredentials [#2](https://github.com/jenkinsci/kubernetes-cli-plugin/pull/2)

1.0.0
-----
* Fix sporadic execution error when running kubectl concurrently

0.1.0
-----
* Import authentication feature from the kubernetes plugin
* Add support for StringCredentials
* Add support for scoped credentials
* Add support for tokens and password with spaces
* Embed certificates into the kubeconfig
* Fix the masks used on the commands to prevent tokens from leaking into the logs
* Display kubectl output in the logs
