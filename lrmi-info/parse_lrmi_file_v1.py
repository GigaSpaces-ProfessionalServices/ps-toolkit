#!/usr/bin/python

import argparse
import csv
import matplotlib.pyplot as plt
import matplotlib.cm as cmx
import matplotlib.colors as colors
import numpy as np

from matplotlib.legend_handler import HandlerLine2D
from matplotlib.backends.backend_pdf import PdfPages
from collections import OrderedDict

headers = ['current time', 'machine', 'pid', 'started', 'all threads count']
stat_filter = list(headers) + ['completed_task_count', 'completed_task_per_sec', 'active_threads_perc']

def process_lrmi_line(text):
    clean = text.strip()
    clean = clean_error_text(clean)
    if (not clean):
        return clean
    clean = clean_first_comma(clean)
    clean = clean_last_char(clean)
    if isinstance(clean, str) and ',' in clean:
        return clean.split(',', 1)
    return clean

def clean_first_comma(text):
    if text[0] == ',':
        text = clean_first_comma(text[1:len(text)])
    return text

def clean_last_char(text):
    last_char = text[len(text) - 1:len(text)]
    if last_char == ',' or last_char.isspace():
        text = clean_last_char(text[0:len(text) - 1])
    return text
    
def clean_error_text(text):
    if text.startswith('ERROR retrieving ThreadInfo: Is LRMI active?'): 
        text = clean_error_text(text[len('ERROR retrieving ThreadInfo: Is LRMI active?'):len(text)])
    return text
        
def process_header_line(text):
    clean = text.strip()
    header = next(header for header in headers if header in clean.lower())
    return (header, clean[len('~~~ ') + len(header) + 1:len(clean)])

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

def parse_arguments():
    parser = argparse.ArgumentParser(description='Parses lrmi-info utility logs and put statistics to csv file.')
    parser.add_argument('filepath', metavar='file_path', type=str, help='path to file to be parsed')
    parser.add_argument('--v', dest='graphs', action='store_true', help='creates graphs in pdf')
     
    return parser.parse_args()

def stat_to_csv_file(uid_to_space_map, uid_lrmi_info_map):
    with open('lrmi_info.csv', 'wb') as lrmi_file:
        wr = csv.writer(lrmi_file, quoting=csv.QUOTE_MINIMAL)
        for uid, space_name in uid_to_space_map.iteritems():
            wr.writerow([space_name, uid])
            
            for stat, values in uid_lrmi_info_map[uid].iteritems():
                row = list(values)
                row.insert(0, stat)
                 
                wr.writerow(row)
            wr.writerow([])
        
def graphs_to_pdf_file(uid_to_space_map, uid_lrmi_info_map):
    i = 1
    stat_count = len(uid_to_space_map)
    plt.figure(i, figsize=(20, 10 * stat_count))
    
    for uid, space_name in uid_to_space_map.iteritems():
        colorMap = get_cmap(len(uid_lrmi_info_map[uid]))
        x = None
        line = None
        j = 0

        plt.subplot(stat_count, 1, i) 
        plt.grid(True)
        plt.title("SpaceName:'{0}' GSC:'{1}'".format(space_name, uid))

        i = i + 1
        for stat, values in uid_lrmi_info_map[uid].iteritems():
            if x is None:
                x = np.arange(0, len(values), 1.0)

            if stat.lower() not in stat_filter:
                yValues = values + ['0']*(len(x) - len(values))
                line, = plt.plot(x, yValues, color=colorMap(j), label=stat.lower())
                plt.legend(handler_map={line: HandlerLine2D(numpoints=4)})
                plt.yscale('log')
                j = j + 1

    plt.tight_layout()
  
    pdf = PdfPages('lrmi_graphs.pdf')
    pdf.savefig()
    
    pdf.close()
    
if __name__ == '__main__':

    gsc_uids = set()
    count = 100
    limit = 1200
    do_limit = False

    current_space_uid = None
    current_processed_time = None

    uid_to_space_map = dict()
    uid_lrmi_info_map = dict()

    skip = False
    
    args = parse_arguments()
    
    for line in file(args.filepath):

        some_uid = None
        some_space_name = None

        count += 1

        if count == limit and do_limit:
            break

        # Look for GSC uid
        if line.find('~~~ GSC') != -1:
            uid = line.split()[2]
            if uid[0] == '[':
                uid = uid[1:len(uid)]
        
            gsc_uids.add(uid)
            current_space_uid = uid
            
            skip = False
            #print 'Detected new GSC {0}'.format(uid)
            continue
        # deal with property followed by 'XXX BUNCH OF TEXT'
        if line.find('XXX') >= 0:
            skip = True
            space_name = look_for_space_name(line)
            if space_name:
                uid_to_space_map[current_space_uid] = space_name
                continue
        else:
            if line.find('~~~') != -1:
                tup = process_header_line(line)
            else:
                tup = process_lrmi_line(line)
        if skip or current_space_uid is None or not isinstance(tup, list): 
            continue
        if  current_space_uid in uid_lrmi_info_map:
            if tup[0] in uid_lrmi_info_map[current_space_uid]:
                uid_lrmi_info_map[current_space_uid][tup[0]].append(tup[1])
            else:
                uid_lrmi_info_map[current_space_uid][tup[0]] = [tup[1]]
        else:
            uid_lrmi_info_map[current_space_uid] = OrderedDict({tup[0]:[tup[1]]})
            
    # end for loop
    stat_to_csv_file(uid_to_space_map, uid_lrmi_info_map)
    
    if args.graphs:
        graphs_to_pdf_file(uid_to_space_map, uid_lrmi_info_map)
