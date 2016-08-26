git to svn interop
==================

The master repository for project work products is a Subversion version
control repository.
If you wish to work with this svn repository using git that can be easily done.

This approach works well if the svn repository is constructed conventionally.
A conventional configuration is one with a /trunk/, /branches/, and /tags/ folder.

   git svn clone -s https://dsl-external.bbn.com/svn/immortals immortals

This will give a git work space only checking out the /trunk/.
Branches are handled as remote branches as can be seen by...

   git branch -a

Synchronizing with subversion has its own commands.
Rather than a 'pull' command...

   git svn rebase

...will apply changes from subversion back to your master.
And, rather than 'push' commits back to the upstream repository...

   git svn dcommit

   
