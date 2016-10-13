#!/usr/bin/python

import sys

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
    for oper, statistics in oper_stat_map.iteritems():
        print oper
        counter = 0
        num_statistics = len(statistics)
        for stat_key, values in statistics.iteritems():
            if counter != num_statistics:
                print ",{0},{1}".format(stat_key, ','.join(values))
            else:
                print ",{0},{1}\n".format(stat_key, ','.join(values))
            counter += 1
