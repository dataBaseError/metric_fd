
require 'pg'

class DBInterface

    USERNAME = "postgresql"
    PASSWORD = "test123"


    def initialize(name)
        @conn = PG.connect(dbname: name, user: USERNAME, password: PASSWORD)
    end

    # x_attributes.class == Array
    # y_attribute.class == String
    def sort_by_x(table, x_attributes, y_attribute)

        attributes = x_attributes << y_attribute
        get_sorted(table, attributes)
    end

    def sort_by_y(table, y_attribute, x_attributes)

        attributes = [y_attribute] + x_attributes
        get_sorted(table, attributes)
    end

private

    def get_sorted(table, attributes)

        attribute_list = attributes.join(", ")

        values = Array.new
        @conn.exec("SELECT #{attribute_list} FROM #{table} GROUP BY #{attribute_list}") do |results|
            results.each_with_index do |row, i|
                values << Hash.new
                row.each_with_index do |element, j|

                    values[i][attributes[j]] = element
                end
            end
        end
    end
end

class Repair

    def initialize(x_attributes, y_attribute, delta)
        @x_attributes = x_attributes
        @y_attribute = y_attribute
        @delta = delta
    end

    def create_groups(rows)
        core_patterns = Array.new

        current_pattern = Array.new
        best_pattern = Array.new

        min_row = 0
        rows.each_with_index do |row, i|

            if i > 0
                in_group = compare_array(rows[i-1], row, @x_attributes)

                if !in_group 
                    # Set the core_pattern to the largets sub-group
                    if best_pattern.size < current_pattern.size
                        best_pattern = current_pattern
                    end
                    core_patterns << best_pattern

                    # Clear the data for the previous groups
                    current_pattern.clear
                    best_pattern.clear
                    min_row = row

                elsif distance(min_row[@y_attribute], row[@y_attribute]) > delta
                    # Update the best group 
                    if best_pattern.size < current_pattern.size
                        best_pattern = current_pattern
                    end
                    # Clear the data for the previous subgroup
                    current_pattern.clear

                    # Update the minimum row
                    min_row = row
                end
                current_pattern << row
            else
                min_row = row
                #core_patterns << [row]
                current_pattern << row
            end
        end

        return core_patterns
    end

    def get_target(g, h, attribute, delta)

        g_attribute = Array.new

        # Get the attribute values in an array
        g.each do |value|
            g_attribute << value[attribute]
        end

        if g[0][attribute] > h[0][attribute]
            return subtract(g_attribute.max, delta)
        else
            return addition(g_attribute.min, delta)
        end
    end

    def cost_analysis_rhs(rows, core_patterns)

        i = 0
        rows.each do |row|
            # Find the next core pattern that matches
            if !compare_array(core_patterns[i].first, row, @x_attributes)
                i += 1
            end

            # Is row a Deviant?
            if !core_patterns[i].include?(row)
                # Repair the right hand side of the row
                result = get_target(core_patterns[i], row, @y_attribute)
                row[@y_attribute] = result

                # The cost would be:
                # cost = distance(row[@y_attribute], get_target(core_patterns[i], row, @y_attribute))
            end
        end
    end

private
    
    def compare_array(element1, element2, attributes)
        # Compare all the attributes
        attributes.each do |attribute|
            if element1[attribute] != attributes[attribute]
                return false
            end
        end
        return true
    end
end

# Levenshtein distance calculation from wikibooks
# website: https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance
# Calculates the number of key strokes difference between the two given strings
def levenshtein(first, second)
  matrix = [(0..first.length).to_a]
  (1..second.length).each do |j|
    matrix << [j] + [0] * (first.length)
  end
 
  (1..second.length).each do |i|
    (1..first.length).each do |j|
      if first[j-1] == second[i-1]
        matrix[i][j] = matrix[i-1][j-1]
      else
        matrix[i][j] = [
          matrix[i-1][j],
          matrix[i][j-1],
          matrix[i-1][j-1],
        ].min + 1
      end
    end
  end
  return matrix.last.last
end

# For the numeric data
def distance(x, y)
    return (x - y).abs
end

def subtract(x, y)
    return x - y
end

def addition(x, y)
    return x + y
end


# For the string data
def distance(x, y)
    return levenshtein(x,y)
end

def subtract(x, y)
    return yield
end

def addition(x, y)
    return yield
end

database_name = "movies"
table_name = "table_name"
delta = 5

x_attributes = ["X"]
y_attribute = "Y"



db = DBInterface.new(database)

results = db.sort_by_x(table_name, x_attributes, y_attribute)

repairer = Repair.new(x_attributes, y_attribute, delta)
repairer.create_groups(results)
