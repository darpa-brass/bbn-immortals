#!/usr/bin/env python3
import argparse
import json
import os
import subprocess
import time
from typing import Dict, List, Optional

_parser = argparse.ArgumentParser()

_parser.add_argument('target_directory', metavar='TARGET_DIRECTORY', type=str,
                     help='The directory where this repository should be published to')
_parser.add_argument('target_branch', metavar="TARGET_BRANCH", type=str,
                     help="The branch that the repository should be pushed to")
_parser.add_argument('--source-branch', type=str, help='The source branch. If not specified, trunk will be used')
_parser.add_argument('-d', '--dry-run', action='store_true',
                     help='Only prints the commands to be executed and doesn\'t run them')
_parser.add_argument('-upr', '--update-primary-ref', type=str,
                     help='The SVN revision everything except the knowledge-repo and dsl should be updated to')
_parser.add_argument('-ukr', '--update-kr-ref', type=str,
                     help='The SVN revision the knowledge-repo should be updated to')
_parser.add_argument('-urr', '--update-resource-dsl-ref', type=str,
                     help='The SVN revision the dsl should be updated to')
_parser.add_argument('-udr', '--update-docs-ref', type=str,
                     help='The SVN revision the documentation should be updated to')
_parser.add_argument('-uts', '--update-translation-service-ref', type=str,
                     help="The SVN revision the xml translation service should be updated to")
_parser.add_argument('-o', '--one-off', action='store_true',
                     help='If provided, it will be pushed to the one-off branch')

SCRIPT_DIRECTORY = os.path.dirname(os.path.realpath(__file__))

local_repo = os.path.abspath(os.path.join(SCRIPT_DIRECTORY, "..", ".."))

swri_deployment_manifest = {
    'primary': {
        'phase3': 'immortals_repo/phase3',
        'shared/tools': 'immortals_repo/shared/tools',
        'shared/tools.sh': 'immortals_repo/shared/tools.sh',
        'shared/utils/swri_debug_collect.sh': 'immortals_repo/shared/utils/swri_debug_collect.sh'
    },
    'dsl': {
        'dsl': 'immortals_repo/dsl'
    },
    'knowledgerepo': {
        'knowledge-repo': 'immortals_repo/knowledge-repo'
    },

    'docs': {
        'README.md': 'README.md',
        'docs/CP/phase3/bbn-swri-integration.md': 'ChallengeProblems/bbn-swri-integration.md',
        'docs/CP/phase3/bamboo_workflow.png': 'ChallengeProblems/bamboo_workflow.png',
        'docs/CP/phase3/evaluation_workflow.png': 'ChallengeProblems/evaluation_workflow.png',
        'docs/CP/phase3/cp_05/bbn-swri-mdl-extensions.md': 'ChallengeProblems/cp_05/bbn-swri-mdl-extensions.md',
        'docs/CP/phase3/cp_05/bbn-swri-mdlroot-coverage.md': 'ChallengeProblems/cp_05/bbn-swri-mdlroot-coverage.md',
        'docs/CP/phase3/cp_05/rules': 'ChallengeProblems/cp_05/rules',
        'docs/CP/phase3/cp_05/description.md': 'ChallengeProblems/cp_05/description.md',
        'docs/CP/phase3/cp_05/example_assumptions/ExampleCoverage.xml': 'ChallengeProblems/cp_05/example_assumptions/ExampleCoverage.xml',
        'docs/CP/phase3/cp_05/CP5-Results-Format.md': 'ChallengeProblems/cp_05/CP5-Results-Format.md',
        'docs/CP/phase3/cp_06/CP6-Results-Format.md': 'ChallengeProblems/cp_06/CP6-Results-Format.md',

    },
    'translationservice': {
        'knowledge-repo/cp/cp3.1/xsd-tranlsation-service-aql':
            'immortals_repo/knowledge-repo/cp/cp3.1/xsd-tranlsation-service-aql'
    }
}


def main():
    args = _parser.parse_args()
    target_dir = args.target_directory
    timestamp = time.strftime('%m-%d-%Y_%H-%M-%S-%Z')
    dry_run = args.dry_run

    def exec_cmd(cmd: List[str], cwd: Optional[str] = None):
        if dry_run:
            print(cmd)
            if cwd is not None:
                print('\t(in directory "' + cwd + '"')

        else:
            if cwd is None:
                subprocess.run(cmd)
            else:
                subprocess.run(cmd, cwd=cwd)

    def deploy(src: str, target: str, revision: str):
        exec_cmd(['svn', 'export', '-r', revision, 'https://dsl-external.bbn.com/svn/immortals/' + source_subdir + src,
                  os.path.join(target_dir, target)])

    def deploy_fileset(src_target_dict: Dict[str, str], revision: str):
        for src in src_target_dict.keys():
            tgt = src_target_dict[src]
            tgt_path = os.path.join(target_dir, tgt)

            if os.path.exists(tgt_path):
                exec_cmd(['rm', '-r', os.path.join(target_dir, tgt)])

            elif not os.path.exists(os.path.dirname(tgt_path)):
                exec_cmd(['mkdir', '-p', os.path.dirname(tgt_path)])

            deploy(src, tgt, revision)

    target_branch = args.target_branch

    if args.source_branch is not None:
        source_subdir = 'branches/' + args.source_branch + '/'
    else:
        source_subdir = 'trunk/'
    primary_ref = args.update_primary_ref
    kr_ref = args.update_kr_ref
    dsl_ref = args.update_resource_dsl_ref
    docs_ref = args.update_docs_ref
    xts_ref = args.update_translation_service_ref

    if primary_ref is None and kr_ref is None and dsl_ref is None and docs_ref is None and xts_ref is None:
        print("No updated revisions have been provided!")
        exit(1)

    if os.path.exists(os.path.join(target_dir, '.git')):
        subprocess.run(['git', 'pull', 'origin', 'master'], cwd=target_dir)
        subprocess.run(['git', 'pull', 'origin', target_branch], cwd=target_dir)
        subprocess.run(['git', 'checkout', target_branch], cwd=target_dir)
        subprocess.run(['git', 'rebase', 'master'], cwd=target_dir)

    if primary_ref is not None:
        deploy_fileset(swri_deployment_manifest['primary'], primary_ref)

    if docs_ref is not None:
        deploy_fileset(swri_deployment_manifest['docs'], docs_ref)

    if kr_ref is not None:
        deploy_fileset(swri_deployment_manifest['knowledgerepo'], kr_ref)

    if dsl_ref is not None:
        deploy_fileset(swri_deployment_manifest['dsl'], dsl_ref)

    if xts_ref is not None:
        deploy_fileset(swri_deployment_manifest['translationservice'], xts_ref)

    info_filepath = os.path.join(target_dir, 'revision_info.json')
    if os.path.isfile(info_filepath):
        previous_revision_info = json.load(open(info_filepath))
        if primary_ref is None:
            primary_ref = previous_revision_info['primary_ref']

        if kr_ref is None:
            kr_ref = previous_revision_info['knowledgerepo_ref']

        if dsl_ref is None:
            dsl_ref = previous_revision_info['dsl_ref']

        if docs_ref is None:
            docs_ref = previous_revision_info['docs_ref']

        if xts_ref is None:
            xts_ref = previous_revision_info['xts_ref']
            deploy_fileset(swri_deployment_manifest['translationservice'], xts_ref)

    # with open(info_filepath, 'w') as f:
    info_dict = {
        "time": timestamp,
        "primary_ref": primary_ref,
        "knowledgerepo_ref": kr_ref,
        "dsl_ref": dsl_ref,
        'docs_ref': docs_ref,
        'xts_ref': xts_ref
    }
    json.dump(info_dict, open(info_filepath, 'w'), indent=4, sort_keys=True)


if __name__ == '__main__':
    main()
