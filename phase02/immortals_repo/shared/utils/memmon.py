#!/usr/bin/env python3
import json
import signal
import subprocess
import time


class ProcessInfo:
    def __init__(self, process_line, timestamp):
        self.timestamp = timestamp

        val = process_line.strip()
        empty = val.find(' ')
        self.pid = val[0:empty]

        val = val[empty:].strip()
        empty = val.find(' ')
        self.pmem = val[0:empty]

        val = val[empty:].strip()
        empty = val.find(' ')
        self.rss = val[0:empty]

        val = val[empty:].strip()
        empty = val.find(' ')
        self.drs = val[0:empty]

        val = val[empty:].strip()
        empty = val.find(' ')
        self.maj_flt = val[0:empty]

        val = val[empty:].strip()
        empty = val.find(' ')
        self.min_flt = val[0:empty]

        val = val[empty:].strip()
        empty = val.find(' ')
        self.sz = val[0:empty]

        val = val[empty:].strip()
        empty = val.find(' ')
        self.vsz = val[0:empty]

        val = val[empty:].strip()
        self.command = val


class ProcessMonitor:
    def __init__(self, interval_s):
        self.start_time = int(time.time() * 1000)
        self.interval_s = interval_s
        self.keep_running = True
        self.process_info = dict()

    def poll(self):
        timestamp = int(time.time() * 1000)
        cmd = ["ps", "-eo", "pid,pmem,rss,drs,maj_flt,min_flt,sz,vsz,command", '--sort=-pmem']
        rval = subprocess.check_output(cmd).decode()  # type: str

        for line in rval.split("\n")[1:11]:
            pi = ProcessInfo(process_line=line, timestamp=timestamp)
            values = self.process_info.get(pi.command)

            if values is None:
                values = list()
                self.process_info[pi.command] = values
            values.append(pi)

    def start(self):
        while self.keep_running:
            self.poll()
            time.sleep(self.interval_s)
        self.save()

    def save(self):
        timestamp = int(time.time() * 1000)

        d = dict()
        for key in self.process_info.keys():
            values = list()
            d[key] = values

            for pi in self.process_info[key]:
                values.append(pi.__dict__)

        with open('memmon-' + str(timestamp) + 'to' + str(self.start_time) + '.json', 'w') as output:
            json.dump(d, output, sort_keys=True, indent=4)

    def finish(self, signal, frame):
        self.keep_running = False


def main():
    pm = ProcessMonitor(interval_s=4)
    signal.signal(signal.SIGINT, pm.finish)
    pm.start()


if __name__ == '__main__':
    main()
