#!/usr/bin/python

import sys
import matplotlib.patches as mpatches
import matplotlib.pyplot as plt
from matplotlib.legend_handler import HandlerLine2D
from matplotlib.backends.backend_pdf import PdfPages
from cProfile import label

file_name = sys.argv[1]

def process_operation(line):
    array = line.strip().split()
    return "{0}{1}{2}{3}{4}".format(array[6], array[9], array[12], array[15], array[19])

def process_statistics(line, suffix=''):
    array = line.strip().split()
    return [array[4] + suffix, array[6]]

if __name__ == '__main__':

    count = 100
    limit = 1200
    do_limit = False

    cur_oper = None
    cur_time = None
    pca = False
    oper_stat_map = dict()

    for line in file(file_name):
        count += 1

        if count == limit and do_limit:
            break

        if line.find('[ps-inspector] XAP IO Statistics') != -1:
            cur_time = line[0:12].replace(",", ".")
            continue

        if line.find('piecewise constant approximation') != -1:
            pca = True
            continue

        if line.find('t-Digest') != -1:
            pca = False
            continue

        stat = None
        if line.find('[ps-inspector] Space') != -1:
            cur_oper = process_operation(line)
            stat = ['wall clock', cur_time]
        elif line.find('[ps-inspector] EMA') != -1 or line.find('[ps-inspector] MIN') != -1 or line.find('[ps-inspector] MAX') != -1:
            stat = process_statistics(line)
        elif line.find('[ps-inspector] MEDIAN') != -1 and not pca:
            stat = process_statistics(line)
        elif line.find('[ps-inspector] P95th') != -1:
            stat = process_statistics(line, ' PCA' if pca else ' TD')
        else:
            continue
        if cur_oper in oper_stat_map:
            if stat[0] in oper_stat_map[cur_oper]:
                oper_stat_map[cur_oper][stat[0]].append(stat[1])
            else:
                oper_stat_map[cur_oper][stat[0]] = [stat[1]]
        else:
            oper_stat_map[cur_oper] = {stat[0]:[stat[1]]}

    # end for loop
    i = 1
    stat_count = len(oper_stat_map)
    plt.figure(1, figsize=(20, 10 * stat_count))
    
    for oper, statistics in oper_stat_map.iteritems():
        print oper
        
        x = None
        median = None
        p95 = None
        ema = None
        min = None
        max = None
        
        for stat_key, values in statistics.iteritems():
            print ",{0},{1}".format(stat_key, ','.join(values))
            
            if x is None:
                x = range(len(values))
            if stat_key in 'MEDIAN':
                median = values
            elif stat_key in 'P95th TD':
                p95 = values
            elif stat_key in 'EMA':
                ema = values
            elif stat_key in 'MIN':
                min = values
            elif stat_key in 'MAX':
                max = values
                
        print "\n"

        plt.subplot(stat_count, 1, i) 
        i = i + 1
        
        medianLine, = plt.plot(x, median, 'b', label="median")
        p95Line, = plt.plot(x, p95, 'g', label = "p95th")
        emaLine, = plt.plot(x, ema, 'r', label = "ema")
        minLine, = plt.plot(x, min, "y", label = "min")
        maxLine, = plt.plot(x, max, "m", label = "max")
        
        plt.legend(handler_map={medianLine: HandlerLine2D(numpoints=4)})
        plt.grid(True)

        operArray = oper.split(',')
        title = "'{0}' space - {1}".format(operArray[0], operArray[1])
        plt.title(title)
        plt.ylabel("ms")
        plt.yscale('log')
        
    pdf = PdfPages('inspector_graphs.pdf')
    pdf.savefig()
    
    pdf.close()