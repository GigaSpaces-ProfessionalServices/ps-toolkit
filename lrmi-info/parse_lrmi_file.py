#!/usr/bin/python

import sys

file_name = sys.argv[1]

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
                    uid_lrmi_info_map[current_space_uid] = {tup[0]:[tup[1]]}    

            # sniff for space name until we find it
            if parse_mode == 2:
                space_name = look_for_space_name(line)
                if space_name:
                    uid_to_space_map[current_space_uid] = space_name
                    parse_mode = 0
    # end for loop
    for uid, space_name in uid_to_space_map.iteritems():
        print "SpaceName,{0},GSC,{1}".format(space_name, uid)
        counter = 0
        num_metrics = len(uid_lrmi_info_map[uid])
        for stat, values in uid_lrmi_info_map[uid].iteritems():
            if counter != num_metrics:
                print ",{0},{1}".format(stat, ','.join(values))
            else:
                print ",{0},{1}\n".format(stat, ','.join(values))
            counter += 1

    print 'Detected {0} GSCs'.format(len(gsc_uids))