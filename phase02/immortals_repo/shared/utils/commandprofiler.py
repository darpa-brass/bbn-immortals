#!/usr/bin/env python3.6

import os
import subprocess
import time
from enum import Enum
from typing import Dict, List

_immortals_root = os.path.abspath(os.path.dirname(os.path.realpath(__file__)) + '/../../') + '/'


class BuildProfile(Enum):
    MartiBuild = (
        os.path.join(_immortals_root, 'applications/server/Marti'),
        [
            [os.path.join(_immortals_root, 'gradlew')]
        ],
        [
            [''],
            ['--daemon']
        ],
        [
            ['clean', 'build'],
            ['validate'],
            ['clean', 'validate']
        ]
    )

    def __init__(self, exec_dir: str, commands: List[List[str]], options: List[List[str]],
                 subcommands: List[List[str]]):
        self.exec_dir = exec_dir
        self.commands = commands
        self.options = options
        self.subcommands = subcommands

    def get_commands(self) -> List[List[str]]:
        rval = list()

        for command in self.commands:

            for option in self.options:

                for subcommand in self.subcommands:
                    rval.append(list(filter(lambda a: a != '', command + option + subcommand)))

        return rval


class CommandProfiler:

    def __init__(self, build_profile: BuildProfile, iterations: int):
        self.build_profile = build_profile
        self.iterations = iterations
        self.results = dict()  # type: Dict[str, List[float]]

    def add_result(self, command: List[str], duration: float):
        command_str = ' '.join(command)
        if command_str not in self.results:
            self.results[command_str] = list()

        self.results[command_str].append(duration)

    def profile(self):
        commands = self.build_profile.get_commands()
        for i in range(self.iterations):
            for command in commands:
                start_time = time.time()
                subprocess.run(command, cwd=self.build_profile.exec_dir)
                self.add_result(command, time.time() - start_time)

        for command in commands:
            command_str = ' '.join(command)
            print('`' + command_str + "`: " + str(sum(self.results[command_str]) / self.iterations))


def main():
    cp = CommandProfiler(BuildProfile.MartiBuild, 4)
    cp.profile()


if __name__ == '__main__':
    main()
