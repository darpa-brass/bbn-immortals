import atexit
import os
import subprocess

from xmltest import IMMORTALS_ROOT
from xmltest.fragments import XmlElement


class XsdTranslationService:
    _CWD = os.path.join(IMMORTALS_ROOT, 'knowledge-repo', 'cp', 'cp3.1', 'xsd-tranlsation-service-aql', 'aql')
    # _START_SCRIPT = 'set -e;source ~/.immortals/anaconda/bin/activate;conda activate aql;python3 server.py'

    _process = None

    @staticmethod
    def start():
        if XsdTranslationService._process is None or XsdTranslationService._process.returncode is not None:
            XsdTranslationService._process = subprocess.Popen(['python3', 'server.py'],
                                                              cwd=XsdTranslationService._CWD)

    @staticmethod
    @atexit.register
    def stop():
        p = XsdTranslationService._process
        if p is not None:
            if p.returncode is None:
                p.terminate()
                p.wait(2)
                if p.returncode is None:
                    p.kill()


class XmlTester:
    def __init__(self):
        pass

    def sanity_test(self):
        XsdTranslationService.start()
        src_xsd = os.path.join(IMMORTALS_ROOT, 'knowledge-repo/cp/cp3.1/cp-ess-min/etc/schemas/v17/MDL_v0_8_17.xsd')
        dst_xsd = os.path.join(IMMORTALS_ROOT, 'knowledge-repo/cp/cp3.1/cp-ess-min/etc/schemas/v19/MDL_v0_8_19.xsd')
        src_xml = os.path.join(IMMORTALS_ROOT, 'knowledge-repo/cp/cp3.1/cp-ess-min/etc/messages/v17/')

        jar_file = '/home/awellman/Downloads/tmp/immortals/xsd-translation-service-tester.jar'

        results_dir = os.path.join(IMMORTALS_ROOT, "TestResults")
        cmd = ['java',
               '-DclientUrl=http://127.0.0.1:8090/xsdsts',
               '-DsrcSchema=' + src_xsd,
               '-DdstSchema=' + dst_xsd,
               '-DsrcDocs=' + src_xml,
               '-DresultsDir=' + results_dir,
               '-jar',
               jar_file
               ]

        print('CMD: [' + '\n'.join(cmd) + ']')
        result = subprocess.run(cmd)

    def execute(self):
        XsdTranslationService.start()

        for scenario in XmlElement:  # type: XmlElement
            src_xsd = scenario.initial_xsd_path
            dst_xsd = scenario.updated_xsd_path

            src_xml = scenario.root_path

            jar_file = '/home/awellman/Downloads/tmp/immortals/xsd-translation-service-tester.jar'

            results_dir = os.path.join(IMMORTALS_ROOT, "TestResults")
            cmd = ['java',
                   '-DclientUrl=http://127.0.0.1:8090/xsdsts',
                   '-DsrcSchema=' + src_xsd,
                   '-DdstSchema=' + dst_xsd,
                   '-DsrcDocs=' + src_xml,
                   '-DresultsDir=' + results_dir,
                   '-jar',
                   jar_file
                   ]

            print('CMD: [' + '\n'.join(cmd) + ']')
            result = subprocess.run(cmd)


if __name__ == '__main__':
    xt = XmlTester()
    xt.sanity_test()
