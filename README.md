# Nike

Export Nike+ running data to GPX

# Installation

## Homebrew (binary)

```
brew tap martijndwars/me && brew install nike
```

## From source

Clone the repository and run `sbt assembly` to create a fat jar in `target/scala-2.11/`.

# Usage

```
nike 1.0
Usage: nike [list|show] <args>...

Command: list [options]
Show all activities
  -bt, --before-time <value>
                           Only show activities before given epoch timestamp
Command: show id
Show single activity
  id                       Activity identifier
```
