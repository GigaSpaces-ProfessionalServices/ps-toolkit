#!/usr/bin/python

import sys
import matplotlib.pyplot as plt
import matplotlib.cm as cmx
import matplotlib.colors as colors
import numpy as np
from matplotlib.legend_handler import HandlerLine2D
from matplotlib.backends.backend_pdf import PdfPages
from collections import OrderedDict

file_name = sys.argv[1]

headers = ['current time', 'machine', 'pid', 'started', 'all threads count']
stat_filter = list(headers) + ['completed_task_count', 'completed_task_per_sec', 'active_threads_perc']

def process_lrmi_line(text):
    clean = text.strip()
    if text[0] == ',':
        clean = text[1:len(text)]

    last_char = clean[len(clean) - 1:len(clean)]
    if last_char == ',' or last_char.isspace():
        clean = clean[0:len(clean) - 1]
        dummy = clean
        return process_lrmi_line(dummy)
    else: 
        if isinstance(clean, str):
            if ',' in clean:
                return clean.split(',', 1)
            elif ':' in clean:
                return clean.split(':', 1)
            else:
                return clean.split(' ',1)
        return clean

def get_cmap(N):
    color_norm = colors.Normalize(vmin=0, vmax=N-1)
    scalar_map = cmx.ScalarMappable(norm=color_norm, cmap='hsv') 
    def map_index_to_rgb_color(index):
        return scalar_map.to_rgba(index)
    return map_index_to_rgb_color

def look_for_space_name(text):
    c_loc = text.find('SpaceImpl')
    if c_loc != -1:
        open_paren_loc = c_loc + len('SpaceImpl')
        close_paren_loc = text.find(')', open_paren_loc)
        name = text[open_paren_loc + 1:close_paren_loc]
        return name
    return None
    
if __name__ == '__main__':
    gsc_uids = set()
    count = 100
    limit = 1200
    do_limit = False
    parse_mode = 0

    current_space_uid = None
    current_processed_time = None

    uid_to_space_map = dict()
    uid_lrmi_info_map = dict()

    for line in file(file_name):

        some_uid = None
        some_space_name = None

        count += 1

        if count == limit and do_limit:
            break

        # Look for GSC uid
        if line.find('GSC:') != -1:

            begin = line.index('[') + 1
            uid = line[begin:len(line)].rstrip()

            gsc_uids.add(uid)
            current_space_uid = uid
            #print 'Detected new GSC {0}'.format(uid)
            parse_mode = 1
        else:

            # deal with property followed by 'XXX BUNCH OF TEXT'
            if parse_mode == 1:
                x_loc = line.find('XXX')
                if x_loc > 0:
                    head = line[0:x_loc]
                    tup = process_lrmi_line(head)
                    parse_mode = 2
                else:
                    if line.find('~~~') == -1:
                        tup = process_lrmi_line(line)
                    else:
                        temp = line[4:len(line)]
                        tup = process_lrmi_line(temp)
                if current_space_uid in uid_lrmi_info_map:
                    if tup[0] in uid_lrmi_info_map[current_space_uid]:
                        uid_lrmi_info_map[current_space_uid][tup[0]].append(tup[1])
                    else:
                        uid_lrmi_info_map[current_space_uid][tup[0]] = [tup[1]]
                else:
                    uid_lrmi_info_map[current_space_uid] = OrderedDict({tup[0]:[tup[1]]})

            # sniff for space name until we find it
            if parse_mode == 2:
                space_name = look_for_space_name(line)
                if space_name:
                    uid_to_space_map[current_space_uid] = space_name
                    parse_mode = 0
    # end for loop
    i = 1
    stat_count = len(uid_to_space_map)
    plt.figure(i, figsize=(20, 10 * stat_count))
    for uid, space_name in uid_to_space_map.iteritems():
        print "SpaceName,{0},GSC,{1}".format(space_name, uid)

        colorMap = get_cmap(len(uid_lrmi_info_map[uid]))
        x = None
        line = None
        j = 0

        plt.subplot(stat_count, 1, i) 
        plt.grid(True)
        plt.title("SpaceName:'{0}' GSC:'{1}'".format(space_name, uid))

        i = i + 1
        for stat, values in uid_lrmi_info_map[uid].iteritems():
            print ",{0},{1}".format(stat, ','.join(values))
            
            if x is None:
                x = np.arange(0, len(values), 1.0)

            if stat.lower() not in stat_filter:
                yValues = values + ['0']*(len(x) - len(values))
                line, = plt.plot(x, yValues, color=colorMap(j), label=stat.lower())
                plt.legend(handler_map={line: HandlerLine2D(numpoints=4)})
                plt.yscale('log')
                j = j + 1
        print "\n"

    pdf = PdfPages('lrmi_graphs.pdf')
    pdf.savefig()
    
    pdf.close()
    print 'Detected {0} GSCs'.format(len(gsc_uids))