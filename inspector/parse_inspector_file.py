#!/usr/bin/python

import argparse
import csv
import matplotlib.patches as mpatches
import matplotlib.pyplot as plt
import matplotlib.cm as cmx
import matplotlib.colors as colors
import numpy as np
import re
from matplotlib.legend_handler import HandlerLine2D
from matplotlib.backends.backend_pdf import PdfPages
from cProfile import label
from collections import OrderedDict

headers = ['EMA', 'MIN', 'MAX', 'MEDIAN', 'P90th', 'P95th', 'P99th', 'P99.9th']
    
def check_optional_perc(perc_key):
    return perc_key in ['P90th', 'P99th', 'P99.9th']
     
def parse_arguments():
    parser = argparse.ArgumentParser(description='Parses lrmi-info utility logs and put statistics to csv file.')
    parser.add_argument('filepath', metavar='file_path', type=str, help='path to file to be parsed')
    parser.add_argument('--v', dest='graphs', action='store_true', help='creates graphs in pdf')
    parser.add_argument('--vv', dest='graphs_perc', action='store_true', help='creates graphs in pdf including 90th, 99th and 99.9th percentiles')
    
    return parser.parse_args()

def get_cmap(N):
    color_norm = colors.Normalize(vmin=0, vmax=N-1)
    scalar_map = cmx.ScalarMappable(norm=color_norm, cmap='hsv') 
    def map_index_to_rgb_color(index):
        return scalar_map.to_rgba(index)
    return map_index_to_rgb_color

def stat_to_csv_file(oper_stat_map):
    with open('inspector.csv', 'wb') as inspector_file:
        wr = csv.writer(inspector_file, quoting=csv.QUOTE_MINIMAL)
        for oper, statistics in oper_stat_map.iteritems():
            wr.writerow([oper])
            for stat_key, values in statistics.iteritems():
                row = list(values)
                row.insert(0, stat_key)
                 
                wr.writerow(row)
            wr.writerow([])

def graphs_to_pdf_file(oper_stat_map):
    i = 1
    stat_count = len(oper_stat_map)
    plt.figure(1, figsize=(20, 10 * stat_count))
    for oper, statistics in oper_stat_map.iteritems():
        colorMap = get_cmap(len(statistics))
        x = None
        line = None
        j = 0
    
        plt.subplot(stat_count, 1, i) 
        plt.grid(True)
        plt.title(oper)
        
        i = i + 1
        for stat_key, values in statistics.iteritems():
            if stat_key in 'current time':
                continue
            if x is None:
                x = np.arange(0, len(values), 1.0)
                
            yValues = values + ['0']*(len(x) - len(values))
            line, = plt.plot(x, yValues, color=colorMap(j), label=stat_key)
            plt.legend(handler_map={line: HandlerLine2D(numpoints=4)})
            plt.yscale('log')
            j = j + 1

    plt.tight_layout()
    
    pdf = PdfPages('inspector_graphs.pdf')
    pdf.savefig()
    
    pdf.close()
    
if __name__ == '__main__':

    count = 100
    limit = 1200
    do_limit = False

    cur_oper = None
    cur_time = None
    pca = False
    oper_stat_map = dict()

    args = parse_arguments()
    graphs_perc = args.graphs_perc
    
    for line in file(args.filepath):
        count += 1

        if count == limit and do_limit:
            break

        if line.find('XAP IO Statistics') != -1:
            cur_time = line[0:12].replace(",", ".")
            continue

        if line.find('piecewise constant approximation') != -1:
            pca = True
            continue

        if line.find('t-Digest') != -1:
            pca = False
            continue

        stat = None
        searchObj = re.search(r'Space = (\w+), operation = (\w+), operationType = (\w+), operationModifier = (\w+), space class = (\w+)', line)
        if searchObj:
            cur_oper = searchObj.group()
            stat = ['current time', cur_time]
        else:  
            for header in headers:
                if pca or (not graphs_perc and check_optional_perc(header)):
                    continue
                searchObj = re.search(r'' + header + '\s*= (.*) ms', line)
                if searchObj:
                    stat = [header, searchObj.group(1)]
                    break
        if not stat:
            continue
        if cur_oper in oper_stat_map:
            if stat[0] in oper_stat_map[cur_oper]:
                oper_stat_map[cur_oper][stat[0]].append(stat[1])
            else:
                oper_stat_map[cur_oper][stat[0]] = [stat[1]]
        else:
            oper_stat_map[cur_oper] = OrderedDict({stat[0]:[stat[1]]})

    # end for loop
    stat_to_csv_file(oper_stat_map)
    
    if args.graphs or graphs_perc:
        graphs_to_pdf_file(oper_stat_map)