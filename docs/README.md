# Upstream Community Theme

The Upstream Community Theme is a ready-to-use [Jekyll](https://jekyllrb.com/) theme to help you create a basic static site for your project. It was designed with the Red Hat Upstream Community in mind, but can be used by anyone looking to create a simple, lightweight site.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development purposes. See deployment for notes on how to deploy the project on [GitHub Pages](https://pages.github.com/).

### Prerequisites

 - Install a full [Ruby development environment](https://www.ruby-lang.org/en/downloads/). Ruby version 2.4.0 or above is required, including all development headers. You can run `ruby -v` to check your current Ruby version.
 - [RubyGems](https://rubygems.org/pages/download). You can run `gem -v` to check if you have RubyGems installed.
 - [GCC](https://gcc.gnu.org/install/) and [Make](https://www.gnu.org/software/make/). You can run `gcc -v`,`g++ -v` and `make -v` to see if your system already has them installed.

### Installing the theme

*[Jekyll documentation pages](https://jekyllrb.com/docs/)*

1. The Jekyll site provides detailed installation instructions for each operating system:
 
  - [Mac](https://jekyllrb.com/docs/installation/macos/)
  - [Linux distributions including Red Hat Linux](https://jekyllrb.com/docs/installation/other-linux)
  - [Ubuntu Linux](https://jekyllrb.com/docs/installation/ubuntu/)
  - [Windows](https://jekyllrb.com/docs/installation/windows/)
    
3. Fork this repository by clicking the _Fork_ button at the top right corner of this page.
4. Clone your fork (please ensure you have current version of git installed) by running: 
  `git clone git@github.com:YOUR_USER_NAME/community-theme.git`
5. Change into the project directory
  `cd community-theme`
6. Build the site and make it available on a local server
  `bundle exec jekyll serve`
7. To preview your site, browse to http://localhost:4000

> If you encounter any unexpected errors during the above, please refer to the [troubleshooting](https://jekyllrb.com/docs/troubleshooting/#configuration-problems) page or the [requirements](https://jekyllrb.com/docs/installation/#requirements) page, as you might be missing development headers or other prerequisites.

_For more information regarding the use of Jekyll, please refer to the [Jekyll Step by Step Tutorial](https://jekyllrb.com/docs/step-by-step/01-setup/)._

## Deployment on GitHub Pages

To deploy your site using GitHub Pages you will need to add the [github-pages gem](https://github.com/github/pages-gem).

> Note that GitHub Pages runs in `safe` mode and only allows a set of [whitelisted plugins](https://help.github.com/articles/configuring-jekyll-plugins/#default-plugins).

To use the github-pages gem, you'll need to add the following on your `Gemfile`:

```
source "https://rubygems.org"
gem "github-pages", group: :jekyll_plugins
```
And then run `bundle update`.

To deploy a project page that is kept in the same repository as the project they are for, please refer to the *Project Pages* section in [Deploying Jekyll to GitHub Pages](https://jekyllrb.com/docs/github-pages/#deploying-jekyll-to-github-pages).


## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on the process for submitting pull requests to us.

## Authors

* [**Adela Sofia A.**](https://github.com/adelasofia) - *Initial theme implementation*
* [**Jason Brock**](https://github.com/jkbrock) - *Visual Designer*

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
