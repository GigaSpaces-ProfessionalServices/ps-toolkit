import os
import csv
import re
import logging
import argparse
import datetime


# list of columns
col_li = ['file name', 'line no.', 'host', 'pid','date time', 'process time', 'gc type/keyword', 'time', 'size before gc', 'size after gc', 'total heap size', 'comment']

# list of directories to visit
dirlist = [r'E:\log\']


def gethomedir():
    return os.path.expanduser('~')
    
def gettimestamp():
    today = datetime.date.today()    
    return today.strftime("%Y%b%d")

output_filename = '{}{}gc-summary-{}.csv'.format(gethomedir(), os.path.sep, gettimestamp())

# list of extensions to visit
extlist = ['.log']

# special patterns to search for
search_li = ['to-space','humongous', r'System.gc\(\)']


# log files were collected and put in directories by hostname, separated by '.'                
def get_hostname(dirpath):
    (head, tail) = os.path.split(dirpath)
    if tail.find('.') > -1:
        (hostname, rest) = tail.split('.', 1)
        logging.debug("hostname: %s", hostname)        
        return hostname
    else:
        return ''

# use Xloggc:/path/to/file/gc.%p.log, where %p tells the JVM to substitute the pid                
def get_pid(filename):
    li = filename.split('pid')
    if( len(li) == 1 ):
        return li[0]
    else:
        (pid, rest) = li[1].split('.', 1)
        logging.debug("pid: %s", pid)
        return pid

        
def myvisitor(extlist, dirname, names):
    global fileinfo
    logging.debug("Current directory: %s", dirname)
    for f in names:
        (p, ext) = os.path.splitext(f)
        logging.debug("%s %s", f, ext)
        if ext in extlist:
            fullpath = os.path.join(dirname, f)
            logging.debug(fullpath)
            try:
                hostname = get_hostname(dirname)
                pid = get_pid(f)
                fileinfo = {'filename': f, 'host' : hostname, 'pid' : pid}
                
                process_file(fullpath)
            except OSError, detail:
                print detail
                
                        
def process_jvminfo(s, linenum):
    s = s.strip()
    mywriter.writerow([fileinfo['filename'], linenum, fileinfo['host'], fileinfo['pid'], '', '', 'jvm info', '', '', '', '', s]) 
        
        
def process_file(fullpath):

    linenum = 0
    f = open(fullpath, 'r')

    # process line by line to get basic information
    for line in f:
        linenum += 1
        # check for keywords of interest
        process_search_pattern(line, linenum)
        if line.startswith('Java HotSpot(TM)') or line.startswith('Memory:') or line.startswith('CommandLine flags:'):
            process_jvminfo(line, linenum)
        elif line.startswith(' ') == False:
            # check for stw pauses that appear on one line
            process_remark_cleanup_fullgc(line, linenum)
          

    # read file object to string. When -XX:+PrintAdaptiveSizePolicy is used,
    # gc phases need a multi-line regex to handle
    # check for stw pause that spans multiple lines
    f.seek(0)
    text = f.read()

    f.close()

    # we are interested in activity that causes a stop-the-world pause and the duration of the gc
    # https://blogs.oracle.com/poonam/entry/understanding_g1_gc_logs

    # process multi-line gc phases
    process_young(text)            
    process_mixed(text)

    
def process_young(s):
    '''
    These gc log statements show up on multiple lines.
    Example:

2017-09-01T16:12:51.133+0000: 134.345: [GC pause (Metadata GC Threshold) (young) (initial-mark)
Desired survivor size 48234496 bytes, new threshold 15 (max 15)
 134.346: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 0, predicted base time: 10.00 ms, remaining time: 990.00 ms, target pause time: 1000.00 ms]
 134.346: [G1Ergonomics (CSet Construction) add young regions to CSet, eden: 63 regions, survivors: 0 regions, predicted young region time: 4209.46 ms]
 134.346: [G1Ergonomics (CSet Construction) finish choosing CSet, eden: 63 regions, survivors: 0 regions, old: 0 regions, predicted pause time: 4219.46 ms, target pause time: 1000.00 ms]
, 0.0325663 secs]

    '''                

    date_time = ''
    process_time = 0.0
    young_type = ''    
    gc_time = 0.0
    
    pattern = re.compile(r'^(\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d\.\d\d\d\+\d\d\d\d): (\d*\.\d*): \[GC pause \(([ \w\.\(\)]*)\) \(young\).+?, (\d*\.\d*) secs\]$', re.MULTILINE | re.DOTALL)
    '''
                            ^ date time
                                                                                   ^ timestamp
                                                                                                          ^type
                                                                                                                                     ^ elapsed time
    '''
    
    for m in pattern.finditer(s):
        date_time = m.group(1)
        process_time = m.group(2)
        young_type = m.group(3)
        gc_time = m.group(4)        
        mywriter.writerow([fileinfo['filename'], '', fileinfo['host'], fileinfo['pid'], date_time, process_time, 'Young generation collection - ' + young_type , gc_time, '', '', '']) 
                
def process_mixed(s):
    '''
2017-09-01T17:53:24.732+0000: 6167.945: [GC pause (G1 Evacuation Pause) (mixed)
Desired survivor size 48234496 bytes, new threshold 1 (max 15)
- age   1:  303167832 bytes,  303167832 total
 6167.945: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 8728, predicted base time: 24.66 ms, remaining time: 975.34 ms, target pause time: 1000.00 ms]
 6167.945: [G1Ergonomics (CSet Construction) add young regions to CSet, eden: 105 regions, survivors: 74 regions, predicted young region time: 305.85 ms]
 6167.945: [G1Ergonomics (CSet Construction) finish adding old regions to CSet, reason: reclaimable percentage not over threshold, old: 19 regions, max: 359 regions, reclaimable: 751186712 bytes (5.00 %), threshold: 5.00 %]
 6167.945: [G1Ergonomics (CSet Construction) finish choosing CSet, eden: 105 regions, survivors: 74 regions, old: 19 regions, predicted pause time: 362.13 ms, target pause time: 1000.00 ms]
 6168.115: [G1Ergonomics (Mixed GCs) do not continue mixed GCs, reason: reclaimable percentage not over threshold, candidate old regions: 335 regions, reclaimable: 751186712 bytes (5.00 %), threshold: 5.00 %]
, 0.1695338 secs]
    '''
    
    process_time = 0.0
    date_time = ''
    gc_time = 0.0

    # output similar to GC pause (young)
    pattern = re.compile(r'^(\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d\.\d\d\d\+\d\d\d\d): (\d*\.\d*): \[GC pause [ \w\(\)]* \(mixed\).+?, (\d*\.\d*) secs\]$', re.MULTILINE | re.DOTALL)
    
    for m in pattern.finditer(s):
        date_time = m.group(1)
        process_time = m.group(2)
        gc_time = m.group(3)        
        mywriter.writerow([fileinfo['filename'], '', fileinfo['host'], fileinfo['pid'], date_time, process_time, 'Mixed generation collection', gc_time, '', '', '']) 

            
def process_remark_cleanup_fullgc(s, linenum):
    '''
    These gc log statements show up on a single line.
    Example:
2017-09-01T16:12:51.175+0000: 134.388: [GC remark 2017-09-01T16:12:51.175+0000: 134.388: [Finalize Marking, 0.0058528 secs] 2017-09-01T16:12:51.181+0000: 134.394: [GC ref-proc, 0.0001349 secs] 2017-09-01T16:12:51.181+0000: 134.394: [Unloading, 0.0032643 secs], 0.0100601 secs]
    
    
44973.856: [GC cleanup 22G->22G(30G), 0.0100070 secs]
 [Times: user=0.08 sys=0.00, real=0.01 secs] 
151413.747: [Full GC151419.349: [SoftReference, 490 refs, 0.0000980 secs]151419.349: [WeakReference, 5036 refs, 0.0004770 secs]151419.349: [FinalReference, 10 refs, 0.0000230 secs]151419.349: [PhantomReference, 129 refs, 346 refs, 0.0000520 secs]151419.349: [JNI Weak Reference, 0.0025470 secs] 19G->19G(30G), 14.2256960 secs] 
    '''
    gc_type = ''
    date_time = ''
    process_time = 0.0
    gc_time = 0.0
    gc_size_before = ''
    gc_size_after = ''
    total_heap_size = ''
                    
    m = re.match(r'^(\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d\.\d\d\d\+\d\d\d\d): (\d*\.\d*): \[GC remark .*, (\d*\.\d*) secs\]$', s)
    if m:
        gc_type = 'GC remark'
        date_time = m.group(1)
        process_time = m.group(2)
        gc_time = m.group(3)     
    else:
        m = re.match(r'^(\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d\.\d\d\d\+\d\d\d\d): (\d*\.\d*): \[GC cleanup .+, (\d*\.\d*) secs\]$', s)
        if m:
            gc_type = 'GC cleanup'
            date_time = m.group(1)
            process_time = m.group(2)
            gc_time = m.group(3)
        else:
            m = re.match(r'^(\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d\.\d\d\d\+\d\d\d\d): (\d*\.\d*): \[Full GC(.+) (\d+[MG])->(\d*[MG])\((\d*[MG])\), (\d*\.\d*) secs\]$', s)
            if m:
                gc_type = 'Full GC'
                date_time = m.group(1)
                process_time    = m.group(2)
                gc_size_before  = m.group(4)
                gc_size_after   = m.group(5)
                total_heap_size = m.group(6)
                gc_time         = m.group(7)

    if gc_type != '':
        mywriter.writerow([fileinfo['filename'], linenum, fileinfo['host'], fileinfo['pid'], date_time, process_time, gc_type, gc_time, gc_size_before, gc_size_after, total_heap_size])

        
def process_search_pattern(s, linenum):
    '''
    Look for search strings of interest. If found write to csv. 
    '''            
    for search_pattern in search_li:
        if re.search(search_pattern, s, re.IGNORECASE):
            s = s.strip()
            mywriter.writerow([fileinfo['filename'], linenum, fileinfo['host'], fileinfo['pid'], '', '', search_pattern, '', '', '', '', s]) 
            break

def process_args():            
    global dirlist, output_filename, host_li

    parser = argparse.ArgumentParser(add_help=False)
    parser.add_argument("--start_dir",  help="the root directory to begin processing")
    parser.add_argument("--output_dir", help="where the output file should be written to. By default the output file will be located in a user's home directory.")
    parser.add_argument("--log_level",  choices=['CRITICAL', 'ERROR', 'WARNING', 'INFO', 'DEBUG'], help="logging level")
    parser.add_argument("--hosts",      help="list of hosts, separated by commas")
    parser.add_argument('-h', '--help', action='help', default=argparse.SUPPRESS, help="This program parses a gc log file and provides a summary in csv format. The following JVM options should be used to generate the log file: -Xloggc:/path/to/file/gc_%%p.log -XX:+PrintCommandLineFlags -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintAdaptiveSizePolicy -XX:+PrintTenuringDistribution -XX:+PrintReferenceGC")
    
    args = parser.parse_args()
    if args.start_dir:
        dirlist = [args.start_dir]
    
    if args.output_dir:
        output_filename = args.output_dir + os.path.sep + "gc_log_summary-" + gettimestamp() + ".csv"
        
    if args.log_level:
        if args.log_level == 'CRITICAL':
            logging.basicConfig(level=logging.CRITICAL)
        elif args.log_level == 'ERROR':
            logging.basicConfig(level=logging.ERROR)
        elif args.log_level == 'INFO':
            logging.basicConfig(level=logging.INFO)
        elif args.log_level == 'DEBUG':
            logging.basicConfig(level=logging.DEBUG)
        else:
            logging.basicConfig(level=logging.WARNING)
    else:
        # set logging level. WARNING is default level
        logging.basicConfig(level=logging.WARNING)

    if args.hosts:
        host_li = args.hosts.split(',')

        
def main():
    global mywriter

    process_args()
    
    # write output to csv file
    with open(output_filename, 'wb') as csvfile:
        mywriter = csv.writer(csvfile)
        # write column headings
        mywriter.writerow(col_li)

        for dir in dirlist:
            logging.debug(dir)
            os.path.walk(dir, myvisitor, extlist)
        
        
main()
