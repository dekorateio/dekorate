# Contributor guidelines

We love contributions! 
Feel free to contribute in any way you like: docs, suggestions, features, fixes tests!

To make both your and our life easier here are some tips:

## editorconfig

To make sure that regardless of IDE/editor everyone uses the same settings we use `editorconfig`. 
Just make sure that you have the `editorconfig` plugin for your editor installed.

## pull request scope

Keep your pull requests as small as possible.
Please, avoid combining code with indentation changes.

## semantic messages

Please use [semantic commit messages](https://seesparkbox.com/foundry/semantic_commit_messages).

## Frequently Asked Questions

* IntelliJ fails to compile dekorate with `Cannot resolve method 'withName(java.lang.String)'` and this kind of errors. 
In order to get dekorate built on IntelliJ you need to manually add generated sources as module sources.
